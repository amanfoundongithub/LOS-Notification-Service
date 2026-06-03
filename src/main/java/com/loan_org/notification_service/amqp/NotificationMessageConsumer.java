package com.loan_org.notification_service.amqp;

import com.loan_org.notification_service.config.RabbitMQConfig;
import com.loan_org.notification_service.dto.NotificationRequest;
import com.loan_org.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationMessageConsumer {

    private final NotificationService notificationService;

    @RabbitListener(
            queues = RabbitMQConfig.HIGH_PRIORITY_QUEUE,
            containerFactory = "highPriorityListenerFactory"
    )
    public void consumeHighPriorityMessage(NotificationRequest message) {
        processMessage(message, "HIGH");
    }

    @RabbitListener(
            queues = RabbitMQConfig.DEFAULT_PRIORITY_QUEUE,
            containerFactory = "defaultPriorityListenerFactory"
    )
    public void consumeMediumPriorityMessage(NotificationRequest message) {
        processMessage(message, "DEFAULT");
    }

    @RabbitListener(
            queues = RabbitMQConfig.BULK_PRIORITY_QUEUE,
            containerFactory = "bulkPriorityListenerFactory"
    )
    public void consumeLowPriorityMessage(NotificationRequest message) {
        processMessage(message, "LOW");
    }

    private void processMessage(NotificationRequest message, String priority) {
        log.info("[AMQP Ingestion] Received {} notification request. Channel: {}, Template: {}, Recipient: {}",
                priority,
                message.getChannel(),
                message.getTemplateCode(),
                maskRecipient(message.getRecipient()));
        try {
            notificationService.execute(message);
        } catch (Exception e) {
            log.error("[AMQP ERROR] Route execution failed. Escalating message directly to DLX.", e);
        }
    }

    private String maskRecipient(String recipient) {
        if (recipient == null) return "UNKNOWN";
        if (recipient.contains("@")) {
            return recipient.replaceAll("(?<=.).(?=[^@]*?.@)", "*");
        }
        return recipient.length() > 4 ? "*".repeat(recipient.length() - 4) + recipient.substring(recipient.length() - 4) : recipient;
    }
}