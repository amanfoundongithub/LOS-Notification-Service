package com.loan_org.notification_service.service.impl;

import com.loan_org.notification_service.document.NotificationLogDocument;
import com.loan_org.notification_service.domain.NotificationStatus;
import com.loan_org.notification_service.dto.NotificationRequest;
import com.loan_org.notification_service.repository.NotificationLogRepository;
import com.loan_org.notification_service.service.NotificationInitiatorService;
import com.loan_org.notification_service.service.NotificationProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationInitiatorServiceImpl implements NotificationInitiatorService {

    private final NotificationLogRepository logRepository;
    private final NotificationProcessingService notificationProcessingService;

    @Override
    public void start(NotificationRequest request) {

        // Check for traceId to ensure traceability of request.
        sanitizeRequest(request);

        // Now we have received the request
        log.info("INFO: Received request to trigger notification to {} from channel {} using " +
                "template {}.",
                request.getRecipient(),
                request.getChannel(),
                request.getTemplateCode());

        // Log this request, saying this is pending
        NotificationLogDocument logEntry = NotificationLogDocument.builder()
                .userId(request.getUserId())
                .transactionId(request.getTransactionId())
                .recipient(request.getRecipient())
                .channel(request.getChannel())
                .templateCode(request.getTemplateCode())
                .priority(request.getPriority())
                .title(request.getTitle())
                .templateVariables(request.getTemplateVariables())
                .status(NotificationStatus.PENDING)
                .build();
        logEntry = logRepository.save(logEntry);

        // Message communicate to the server, saying persistence done
        log.info("INFO: Successfully persisted the audit request to MongoDB. Assigned MongoID: {}. " +
                        "Starting communication now...",
                logEntry.getId());

        // Process the remaining notification now
        notificationProcessingService.process(request);

    }

    private void sanitizeRequest(NotificationRequest request) {
        if(request.getTraceId() == null || request.getTraceId().isBlank()) {
            String traceId = UUID.randomUUID().toString();
            log.warn("WARNING: traceId not set for the request. Completing the request with random traceId. Assigned ID: {}",
                    traceId);
            request.setTraceId(traceId);
        }
    }

}
