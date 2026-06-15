package com.loan_org.notification_service.delivery_service.service.impl;

import com.loan_org.notification_service.domain.audit.entity.NotificationLogDocument;
import com.loan_org.notification_service.domain.audit.service.NotificationLogService;
import com.loan_org.notification_service.domain.dispatcher.NotificationDispatcher;
import com.loan_org.notification_service.delivery_service.dto.NotificationDeliveryRequest;
import com.loan_org.notification_service.delivery_service.service.NotificationDeliveryService;
import com.loan_org.notification_service.shared.exception.NotificationDeliveryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.loan_org.notification_service.shared.util.MaskingUtil.maskRecipient;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationDeliveryServiceImpl implements NotificationDeliveryService {

    private final NotificationLogService logService;
    private final NotificationDispatcher dispatcher;

    private static final long   INITIAL_BACKOFF_MS = 1000;
    private static final double BACKOFF_MULTIPLIER = 2.0;

    @Override
    public void execute(NotificationDeliveryRequest request) {

        log.info("Executing Notification Delivery via {} with templateCode: {}. Recipient: {}",
                request.getChannel(),
                request.getTemplateCode(),
                maskRecipient(request.getRecipient(), request.getChannel()));
        NotificationLogDocument logEntry = logService.createRecord(request);

        log.info("Created Audit record for notification with MongoID: {}. Executing dispatcher...",
                logEntry.getId());
        int currentAttempts = logEntry.getRetryCount() % logEntry.getMaxRetries();
        int maxAttempts = logEntry.getMaxRetries();
        long backoffDelay = INITIAL_BACKOFF_MS;

        while (currentAttempts <= maxAttempts) {

            try {
                if (currentAttempts > 0) {
                    logEntry = logService.updateRetryTelemetry(logEntry.getId(),
                            currentAttempts, backoffDelay);
                }

                log.info("Attempt: {}/{} for dispatching the notification via dispatcher...",
                        currentAttempts + 1, maxAttempts + 1);
                String providerRefId = dispatcher.dispatch(request);

                // ALL IS WELL!
                logService.updateStatusToSent(logEntry.getId(), providerRefId);
                log.info("Notification sent successfully to {} after {} attempts.",
                        maskRecipient(logEntry.getRecipient(), request.getChannel()),
                        currentAttempts + 1);
                return;

            } catch (Exception e) {
                log.warn("Execution iteration #{} crashed. Reason: {}", currentAttempts + 1, e.getMessage());
                currentAttempts++;

                if (currentAttempts > maxAttempts) {
                    log.error("All attempts have been exhausted for sending request. Setting status to FAILED for delivery");
                    logService.updateStatusToFailed(logEntry.getId(), e);
                    throw e;
                }

                try {
                    log.info("Throttling thread pipeline. Sleeping for {} ms before next dispatch...", backoffDelay);
                    Thread.sleep(backoffDelay);
                } catch (InterruptedException ie) {
                    log.error("Local backoff pause sequence has been violated.", ie);
                    logService.updateStatusToFailed(logEntry.getId(), ie);
                    Thread.currentThread().interrupt();
                    throw new NotificationDeliveryException("Thread interrupted during automatic backoff processing phase. Reason:" + ie.getMessage());
                }
                backoffDelay = (long) (backoffDelay * BACKOFF_MULTIPLIER);
            }
        }

    }
}
