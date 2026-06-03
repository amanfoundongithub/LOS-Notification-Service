package com.loan_org.notification_service.service.impl;

import com.loan_org.notification_service.domain.audit.NotificationLogDocument;
import com.loan_org.notification_service.domain.audit.NotificationLogService;
import com.loan_org.notification_service.domain.channels.NotificationDispatcher;
import com.loan_org.notification_service.dto.NotificationRequest;
import com.loan_org.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationLogService logService;
    private final NotificationDispatcher dispatcher;

    private static final long INITIAL_BACKOFF_MS = 1000;
    private static final double BACKOFF_MULTIPLIER = 2.0;

    @Override
    public void execute(NotificationRequest request) {

        log.info("[SERVICE][START] Starting request to trigger notification from channel {} using " +
                        "template {}.",
                request.getChannel(),
                request.getTemplateCode());

        // Record created in DB
        NotificationLogDocument logEntry = logService.createRecord(request);
        log.info("INFO: Successfully persisted the audit record to MongoDB. Assigned MongoID: {}. " +
                        "Starting communication now...",
                logEntry.getId());

        // Now process since we got the record persistent
        int currentAttempts = logEntry.getRetryCount();
        int maxAttempts = logEntry.getMaxRetries();
        long backoffDelay = INITIAL_BACKOFF_MS;

        while (currentAttempts <= maxAttempts) {
            try {
                if (currentAttempts > 0) {
                    logEntry = logService.updateRetryTelemetry(logEntry.getId(),
                            currentAttempts, backoffDelay);
                }

                log.info("[SERVICE][EXECUTE] Routing delivery request to dispatcher. Attempt: {}/{}",
                        currentAttempts + 1, maxAttempts + 1);
                String providerRefId = dispatcher.routeAndDispatch(request);

                logService.updateStatusToSent(logEntry.getId(), providerRefId);
                log.info("[SERVICE][SUCCESS] Notification processed cleanly on attempt count index: {}", currentAttempts);
                return;

            } catch (Exception e) {
                log.warn("[SERVICE][WARN] Execution iteration {} crashed. Error: {}", currentAttempts + 1, e.getMessage());
                currentAttempts++;

                if (currentAttempts > maxAttempts) {
                    log.error("[SERVICE][FATAL] Maximum localized retry bounds exhausted ({}/{}). Committing permanent failure.",
                            currentAttempts, maxAttempts);
                    logService.updateStatusToFailed(logEntry.getId(), e);
                    throw e;
                }

                try {
                    log.info("[SERVICE][SLEEP] Throttling thread pipeline. Sleeping for {} ms before next dispatch...", backoffDelay);
                    Thread.sleep(backoffDelay);
                } catch (InterruptedException ie) {
                    log.error("[SERVICE][INTERRUPTED] local backoff pause sequence broken violently.", ie);
                    logService.updateStatusToFailed(logEntry.getId(), ie);
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Thread interrupted during automatic backoff processing phase", ie);
                }
                backoffDelay = (long) (backoffDelay * BACKOFF_MULTIPLIER);
            }
        }



    }
}
