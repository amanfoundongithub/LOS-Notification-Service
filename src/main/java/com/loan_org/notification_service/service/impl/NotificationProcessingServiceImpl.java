package com.loan_org.notification_service.service.impl;

import com.loan_org.notification_service.document.NotificationLogDocument;
import com.loan_org.notification_service.domain.NotificationStatus;
import com.loan_org.notification_service.dto.NotificationRequest;
import com.loan_org.notification_service.repository.NotificationLogRepository;
import com.loan_org.notification_service.service.NotificationClosingService;
import com.loan_org.notification_service.service.NotificationProcessingService;
import com.loan_org.notification_service.strategy.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationProcessingServiceImpl implements NotificationProcessingService {

    private final NotificationLogRepository logRepository;
    private final NotificationDispatcher dispatcher;
    private final NotificationClosingService closingService;

    @Override
    public void process(NotificationRequest request) {

        // Log that we are dispatching the process
        log.info("INFO: Dispatching the request (traceId: {}) using channel {}...",
                request.getTraceId(),
                request.getChannel());

        // Now ask the MongoDB
        Optional<NotificationLogDocument> isLogEntry = logRepository.findByTransactionId(request.getTransactionId());

        // If not there
        if(isLogEntry.isEmpty()) {
            log.warn("WARNING: Provided transaction Id: {} is not found in Mongo, skipping notification.",
                    request.getTransactionId());
            return;
        }

        // Otherwise, call that we are processing.
        NotificationLogDocument logEntry = isLogEntry.get();
        logEntry.setStatus(NotificationStatus.PROCESSING);
        logRepository.save(logEntry);

        log.info("INFO: Processing request with transactionId: {}",
                request.getTransactionId());

        // Now dispatch the process
        dispatcher.routeAndDispatch(request);

        // Now we will close the request
        closingService.complete(request);

    }

}
