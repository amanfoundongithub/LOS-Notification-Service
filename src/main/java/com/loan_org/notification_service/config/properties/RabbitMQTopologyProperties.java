package com.loan_org.notification_service.config.properties;

import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQTopologyProperties {

    private RabbitMQTopologyProperties() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // --- Core Exchanges ---
    public static final String EXCHANGE_NAME = "notification.exchange";
    public static final String DEAD_LETTER_EXCHANGE = "notification.dlx";

    // --- Core Queues ---
    public static final String HIGH_PRIORITY_QUEUE = "high-priority.queue";
    public static final String DEFAULT_PRIORITY_QUEUE = "default-priority.queue";
    public static final String BULK_PRIORITY_QUEUE = "bulk-priority.queue";
    public static final String DEAD_LETTER_QUEUE = "notification.dlq";

    // --- Routing Keys ---
    public static final String ROUTING_KEY_HIGH = "notification.*.high";
    public static final String ROUTING_KEY_DEFAULT = "notification.*.medium";
    public static final String ROUTING_KEY_LOW = "notification.*.low";
    public static final String DEAD_LETTER_ROUTING_KEY = "notification.dlq.routing-key";

    // --- RabbitMQ Argument Keys ---
    public static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
    public static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";


}
