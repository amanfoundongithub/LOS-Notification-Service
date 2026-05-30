package com.loan_org.notification_service.service;

import com.loan_org.notification_service.document.NotificationLogDocument;
import com.loan_org.notification_service.domain.NotificationPriority;
import com.loan_org.notification_service.domain.NotificationStatus;
import com.loan_org.notification_service.dto.NotificationRequest;
import com.loan_org.notification_service.repository.NotificationLogRepository;
import com.loan_org.notification_service.strategy.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationLogService {

    // Inject the services for logging and RabbitMQ
    private final NotificationLogRepository logRepository;
    private final NotificationDispatcher dispatcher;

    /**
     * Initiates notification logging and request using external
     * providers.
     */
    public NotificationLogDocument initiateNotification(NotificationRequest request) {

        // Logging that we are starting the process
        log.info("Received notification request for User: {}, Channel: {}, Template: {}. Starting logging now...",
                request.getUserId(),
                request.getChannel(),
                request.getTemplateCode());

        // Save to MongoDB
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

        // Log the saving to Mongodb
        log.info("Successfully persisted the record to MongoDB with id: {}! Sending message to the user now...",
                logEntry.getId());

        try {
            // Map to the queue
            String routingQueue = getQueue(request.getPriority());

            // Now we will dispatch the email
            dispatcher.routeAndDispatch(request);

        } catch (Exception e) {
            log.error("Critical failure queuing notification log ID {} to RabbitMQ. Marking for downstream retry.",
                    logEntry.getId(), e);

            // Update cache for reference
            logEntry.setStatus(NotificationStatus.FAILED);
            logEntry.setErrorCode("AMQP_DISPATCH_FAILURE");
            logEntry.setErrorMessage(e.getMessage());
            logRepository.save(logEntry);
        }

        return logEntry;
    }

    @Transactional
    public void markAsDelivered(String logId, String providerRefId) {
        logRepository.findById(logId).ifPresent(logEntry -> {
            logEntry.setStatus(NotificationStatus.DELIVERED);
            logEntry.setProviderReferenceId(providerRefId);
            logEntry.setSentAt(java.time.Instant.now());
            logRepository.save(logEntry);
            log.info("Notification log ID {} officially marked as DELIVERED by remote gateway.", logId);
        });
    }

    private String getQueue(NotificationPriority priority) {
        return "notification.routing." + priority.toString().toLowerCase();
    }
}
