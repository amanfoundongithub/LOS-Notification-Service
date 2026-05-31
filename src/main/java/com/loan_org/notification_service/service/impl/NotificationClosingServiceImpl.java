package com.loan_org.notification_service.service.impl;

import com.loan_org.notification_service.document.NotificationLogDocument;
import com.loan_org.notification_service.domain.NotificationStatus;
import com.loan_org.notification_service.dto.NotificationRequest;
import com.loan_org.notification_service.repository.NotificationLogRepository;
import com.loan_org.notification_service.service.NotificationClosingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationClosingServiceImpl implements NotificationClosingService {

    private final NotificationLogRepository logRepository;

    @Override
    public void complete(NotificationRequest request) {
        // Log
        log.info("INFO: Notification service completed for {}",
                request.getTraceId());

        // Now ask the MongoDB
        Optional<NotificationLogDocument> isLogEntry = logRepository.findByTransactionId(request.getTransactionId());

        // If not there
        if(isLogEntry.isEmpty()) {
            log.warn("WARNING: Provided transaction Id: {} is not found in Mongo, skipping this step.",
                    request.getTransactionId());
            return;
        }

        // Otherwise, call that we are processing.
        NotificationLogDocument logEntry = isLogEntry.get();
        logEntry.setStatus(NotificationStatus.DELIVERED);
        logRepository.save(logEntry);
    }
}
