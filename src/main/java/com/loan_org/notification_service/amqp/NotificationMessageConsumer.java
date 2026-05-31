package com.loan_org.notification_service.amqp;

import com.loan_org.notification_service.config.RabbitMQConfig;
import com.loan_org.notification_service.dto.NotificationRequest;
import com.loan_org.notification_service.service.NotificationInitiatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageConsumer {

    private final NotificationInitiatorService notificationInitiatorService;

    @RabbitListener(queues = RabbitMQConfig.HIGH_PRIORITY_QUEUE)
    public void consumeHighPriorityMessage(NotificationRequest message) {
        log.info("Received HIGH priority notification from queue. Log ID: {}", message.getTraceId());
        processMessage(message);
    }

    @RabbitListener(queues = RabbitMQConfig.DEFAULT_PRIORITY_QUEUE)
    public void consumeMediumPriorityMessage(NotificationRequest message) {
        log.info("Received MEDIUM priority notification from queue. Log ID: {}", message.getTraceId());
        processMessage(message);
    }

    @RabbitListener(queues = RabbitMQConfig.BULK_PRIORITY_QUEUE)
    public void consumeLowPriorityMessage(NotificationRequest message) {
        log.info("Received LOW priority notification from queue. Log ID: {}", message.getTraceId());
        processMessage(message);
    }

    private void processMessage(NotificationRequest message) {
        try {
            log.info("Received request for processing...");
            notificationInitiatorService.start(message);
        } catch (Exception e) {
            log.error("Failed executing message delivery route for log ID: {}", message.getTraceId(), e);
            throw e; // Re-throw to trigger your Dead Letter Queue configuration!
        }
    }
}