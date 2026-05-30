package com.loan_org.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

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

    // --- DLX Configuration Beans ---
    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(DEAD_LETTER_ROUTING_KEY);
    }

    // --- Main Routing Configuration Beans ---
    @Bean
    public TopicExchange mainExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue highPriorityQueue() {
        return QueueBuilder.durable(HIGH_PRIORITY_QUEUE)
                .withArgument(X_DEAD_LETTER_EXCHANGE, DEAD_LETTER_EXCHANGE)
                .withArgument(X_DEAD_LETTER_ROUTING_KEY, DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue defaultPriorityQueue() {
        return QueueBuilder.durable(DEFAULT_PRIORITY_QUEUE)
                .withArgument(X_DEAD_LETTER_EXCHANGE, DEAD_LETTER_EXCHANGE)
                .withArgument(X_DEAD_LETTER_ROUTING_KEY, DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue bulkPriorityQueue() {
        return QueueBuilder.durable(BULK_PRIORITY_QUEUE)
                .withArgument(X_DEAD_LETTER_EXCHANGE, DEAD_LETTER_EXCHANGE)
                .withArgument(X_DEAD_LETTER_ROUTING_KEY, DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    // --- Queue-to-Exchange Bindings ---
    @Bean
    public Binding bindHighPriority() {
        return BindingBuilder.bind(highPriorityQueue()).to(mainExchange()).with(ROUTING_KEY_HIGH);
    }

    @Bean
    public Binding bindDefaultPriority() {
        return BindingBuilder.bind(defaultPriorityQueue()).to(mainExchange()).with(ROUTING_KEY_DEFAULT);
    }

    @Bean
    public Binding bindBulkPriority() {
        return BindingBuilder.bind(bulkPriorityQueue()).to(mainExchange()).with(ROUTING_KEY_LOW);
    }

    // --- JSON Utility Transformer ---
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}