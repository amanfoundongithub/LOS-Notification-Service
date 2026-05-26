package com.loan_org.notification_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange name
    public static final String EXCHANGE_NAME = "notification.exchange";

    // Queues
    public static final String HIGH_PRIORITY_QUEUE = "high-priority.queue";
    public static final String DEFAULT_PRIORITY_QUEUE = "default-priority.queue";
    public static final String BULK_PRIORITY_QUEUE = "bulk-priority.queue";
    public static final String DEAD_LETTER_QUEUE = "notification.dlq";

    // Routing Keys (Using Topic Wildcards)
    public static final String ROUTING_KEY_HIGH = "notification.*.high";
    public static final String ROUTING_KEY_DEFAULT = "notification.*.medium";
    public static final String ROUTING_KEY_LOW = "notification.*.low";

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange("notification.dlx");
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with("notification.dlq.routing-key");
    }

    @Bean
    public TopicExchange mainExchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue highPriorityQueue() {
        return QueueBuilder.durable(HIGH_PRIORITY_QUEUE)
                .withArgument("x-dead-letter-exchange", "notification.dlx")
                .withArgument("x-dead-letter-routing-key", "notification.dlq.routing-key")
                .build();
    }

    @Bean
    public Queue defaultPriorityQueue() {
        return QueueBuilder.durable(DEFAULT_PRIORITY_QUEUE)
                .withArgument("x-dead-letter-exchange", "notification.dlx")
                .withArgument("x-dead-letter-routing-key", "notification.dlq.routing-key")
                .build();
    }

    @Bean
    public Queue bulkPriorityQueue() {
        return QueueBuilder.durable(BULK_PRIORITY_QUEUE)
                .withArgument("x-dead-letter-exchange", "notification.dlx")
                .withArgument("x-dead-letter-routing-key", "notification.dlq.routing-key")
                .build();
    }

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

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}