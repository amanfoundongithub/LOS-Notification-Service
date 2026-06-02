package com.loan_org.notification_service.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.MDC;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@Slf4j
@RequiredArgsConstructor
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
    private static final String X_DEAD_LETTER_EXCHANGE = "x-dead-letter-exchange";
    private static final String X_DEAD_LETTER_ROUTING_KEY = "x-dead-letter-routing-key";

    // --- MDC Parameters ---
    public static final String RABBITMQ_MDC_KEY = "traceId";
    public static final String RABBITMQ_MDC_HEADER = "X-Trace-Id";

    // =========================================================================
    // TOPOLOGY CONFIGURATION (Exchanges, Queues, Bindings)
    // =========================================================================

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

    // =========================================================================
    // INFRASTRUCTURE CONFIGURATION (Converters, Interceptors, Factories)
    // =========================================================================

    @Bean
    public Jackson2JsonMessageConverter consumerJackson2MessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }

    @Bean
    public MethodInterceptor amqpMdcInterceptor() {
        return invocation -> {
            Object[] args = invocation.getArguments();
            String traceId = null;
            if (args.length > 0 && args[0] instanceof Message message) {
                Object header = message.getMessageProperties().getHeaders().get(RABBITMQ_MDC_HEADER);
                if (header != null) traceId = header.toString();
            }
            if (traceId == null) {
                traceId = UUID.randomUUID().toString();
                log.warn("[AMQP][MDC] No traceId found in incoming AMQP message. Generated traceId: {}. Keep this handy for reference", traceId);
            }
            MDC.put(RABBITMQ_MDC_KEY, traceId);
            try {
                return invocation.proceed();
            } finally {
                MDC.clear();
            }
        };
    }

    @Bean
    public SimpleRabbitListenerContainerFactory highPriorityListenerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter,
            MethodInterceptor amqpMdcInterceptor) {
        SimpleRabbitListenerContainerFactory factory = createBasicFactory(connectionFactory, converter, amqpMdcInterceptor);
        return configureSizing(factory, 5, 10, 1);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory defaultPriorityListenerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter,
            MethodInterceptor amqpMdcInterceptor) {
        SimpleRabbitListenerContainerFactory factory = createBasicFactory(connectionFactory, converter, amqpMdcInterceptor);
        return configureSizing(factory, 2, 5, 5);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory bulkPriorityListenerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter,
            MethodInterceptor amqpMdcInterceptor) {
        SimpleRabbitListenerContainerFactory factory = createBasicFactory(connectionFactory, converter, amqpMdcInterceptor);
        return configureSizing(factory, 1, 2, 20);
    }

    private SimpleRabbitListenerContainerFactory createBasicFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter,
            MethodInterceptor amqpMdcInterceptor) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        factory.setAdviceChain(amqpMdcInterceptor);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return factory;
    }

    private SimpleRabbitListenerContainerFactory configureSizing(
            SimpleRabbitListenerContainerFactory factory,
            int concurrentConsumers,
            int maxConcurrentConsumers,
            int preFetchCount
    ) {
        factory.setConcurrentConsumers(concurrentConsumers);
        factory.setMaxConcurrentConsumers(maxConcurrentConsumers);
        factory.setPrefetchCount(preFetchCount);
        return factory;
    }
}