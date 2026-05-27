package com.loan_org.notification_service.amqp;

import com.loan_org.notification_service.config.RabbitMQConfig;
import com.loan_org.notification_service.document.NotificationLogDocument;
import com.loan_org.notification_service.strategy.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageConsumer {

    private final NotificationDispatcher notificationDispatcher;

    @RabbitListener(queues = RabbitMQConfig.HIGH_PRIORITY_QUEUE)
    public void consumeHighPriorityMessage(NotificationLogDocument message) {
        log.info("Received HIGH priority notification from queue. Log ID: {}", message.getId());
        processMessage(message);
    }

    @RabbitListener(queues = RabbitMQConfig.DEFAULT_PRIORITY_QUEUE)
    public void consumeMediumPriorityMessage(NotificationLogDocument message) {
        log.info("Received MEDIUM priority notification from queue. Log ID: {}", message.getId());
        processMessage(message);
    }

    @RabbitListener(queues = RabbitMQConfig.BULK_PRIORITY_QUEUE)
    public void consumeLowPriorityMessage(NotificationLogDocument message) {
        log.info("Received LOW priority notification from queue. Log ID: {}", message.getId());
        processMessage(message);
    }

    private void processMessage(NotificationLogDocument message) {
        try {
            log.info("Bypassing template evaluation. Passing raw content straight to dispatcher for log ID: {}", message.getId());

            // Check if content is empty, fall back to a safe placeholder if none was passed
            if (message.getContent() == null || message.getContent().isBlank()) {
                message.setContent("Notification Alert from LoanOrg System.");
            }

            // Route message containing raw text content straight to our active strategy handlers!
            notificationDispatcher.routeAndDispatch(message);

        } catch (Exception e) {
            log.error("Failed executing message delivery route for log ID: {}", message.getId(), e);
            throw e; // Re-throw to trigger your Dead Letter Queue configuration!
        }
    }
}