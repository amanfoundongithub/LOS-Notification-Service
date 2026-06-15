package com.loan_org.notification_service.amqp;

import com.loan_org.notification_service.config.properties.RabbitMQTopologyProperties;
import com.loan_org.notification_service.dto.NotificationRequest;
import com.loan_org.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import static com.loan_org.notification_service.shared.util.MaskingUtil.maskRecipient;

@Component
@Slf4j
@RequiredArgsConstructor
public class NotificationMessageConsumer {

    private final NotificationService notificationService;

    @RabbitListener(
            queues = RabbitMQTopologyProperties.HIGH_PRIORITY_QUEUE,
            containerFactory = "highPriorityListenerFactory"
    )
    public void consumeHighPriorityMessage(NotificationRequest message) {
        processMessage(message);
    }

    @RabbitListener(
            queues = RabbitMQTopologyProperties.DEFAULT_PRIORITY_QUEUE,
            containerFactory = "defaultPriorityListenerFactory"
    )
    public void consumeMediumPriorityMessage(NotificationRequest message) {
        processMessage(message);
    }

    @RabbitListener(
            queues = RabbitMQTopologyProperties.BULK_PRIORITY_QUEUE,
            containerFactory = "bulkPriorityListenerFactory"
    )
    public void consumeLowPriorityMessage(NotificationRequest message) {
        processMessage(message);
    }

    private void processMessage(NotificationRequest message) {
        log.info("Received notification request to send via channel {} to {}. [Priority: {}]",
                message.getChannel(),
                maskRecipient(message.getRecipient(), message.getChannel()),
                message.getPriority());
        try {
            notificationService.execute(message);
        } catch (Exception e) {
            log.error("Error during processing of the incoming request. Escalating to Dead-Letter-Queue for admin review.", e);
        }
    }
}