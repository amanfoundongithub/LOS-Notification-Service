package com.loan_org.notification_service.config;

import com.loan_org.notification_service.config.properties.RabbitMQListenerFactoryProperties;
import com.loan_org.notification_service.config.properties.RabbitMQTopologyProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.MDC;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RabbitMQConfig {

    // --- RabbitMQ Configuration of factory listeners
    private final RabbitMQListenerFactoryProperties rabbitMQListenerFactoryProperties;

    // --- MDC Parameters ---
    public static final String RABBITMQ_MDC_KEY = "traceId";
    public static final String RABBITMQ_MDC_HEADER = "X-Trace-Id";

    // =========================================================================
    // TOPOLOGY CONFIGURATION (Exchanges, Queues, Bindings)
    // =========================================================================

    @Bean
    public TopicExchange deadLetterExchange() {
        return new TopicExchange(RabbitMQTopologyProperties.DEAD_LETTER_EXCHANGE);
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(RabbitMQTopologyProperties.DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue())
                .to(deadLetterExchange())
                .with(RabbitMQTopologyProperties.DEAD_LETTER_ROUTING_KEY);
    }

    @Bean
    public TopicExchange mainExchange() {
        return new TopicExchange(RabbitMQTopologyProperties.EXCHANGE_NAME);
    }

    @Bean
    public Queue highPriorityQueue() {
        return QueueBuilder.durable(RabbitMQTopologyProperties.HIGH_PRIORITY_QUEUE)
                .withArgument(RabbitMQTopologyProperties.X_DEAD_LETTER_EXCHANGE, RabbitMQTopologyProperties.DEAD_LETTER_EXCHANGE)
                .withArgument(RabbitMQTopologyProperties.X_DEAD_LETTER_ROUTING_KEY, RabbitMQTopologyProperties.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue defaultPriorityQueue() {
        return QueueBuilder.durable(RabbitMQTopologyProperties.DEFAULT_PRIORITY_QUEUE)
                .withArgument(RabbitMQTopologyProperties.X_DEAD_LETTER_EXCHANGE, RabbitMQTopologyProperties.DEAD_LETTER_EXCHANGE)
                .withArgument(RabbitMQTopologyProperties.X_DEAD_LETTER_ROUTING_KEY, RabbitMQTopologyProperties.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue bulkPriorityQueue() {
        return QueueBuilder.durable(RabbitMQTopologyProperties.BULK_PRIORITY_QUEUE)
                .withArgument(RabbitMQTopologyProperties.X_DEAD_LETTER_EXCHANGE, RabbitMQTopologyProperties.DEAD_LETTER_EXCHANGE)
                .withArgument(RabbitMQTopologyProperties.X_DEAD_LETTER_ROUTING_KEY, RabbitMQTopologyProperties.DEAD_LETTER_ROUTING_KEY)
                .build();
    }

    @Bean
    public Binding bindHighPriority() {
        return BindingBuilder.bind(highPriorityQueue())
                .to(mainExchange())
                .with(RabbitMQTopologyProperties.ROUTING_KEY_HIGH);
    }

    @Bean
    public Binding bindDefaultPriority() {
        return BindingBuilder.bind(defaultPriorityQueue()).
                to(mainExchange()).
                with(RabbitMQTopologyProperties.ROUTING_KEY_DEFAULT);
    }

    @Bean
    public Binding bindBulkPriority() {
        return BindingBuilder.bind(bulkPriorityQueue())
                .to(mainExchange())
                .with(RabbitMQTopologyProperties.ROUTING_KEY_LOW);
    }

    // =========================================================================
    // INFRASTRUCTURE CONFIGURATION (Converters, Interceptors, Factories)
    // =========================================================================

    @Bean
    public JacksonJsonMessageConverter jacksonJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
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
            JacksonJsonMessageConverter converter,
            MethodInterceptor amqpMdcInterceptor) {
        SimpleRabbitListenerContainerFactory factory = createBasicFactory(connectionFactory, converter, amqpMdcInterceptor);
        return configureSizing(factory,
                rabbitMQListenerFactoryProperties.getHigh().getConcurrentConsumers(),
                rabbitMQListenerFactoryProperties.getHigh().getMaxConcurrentConsumers(),
                rabbitMQListenerFactoryProperties.getHigh().getPreFetchCount());
    }

    @Bean
    public SimpleRabbitListenerContainerFactory defaultPriorityListenerFactory(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter converter,
            MethodInterceptor amqpMdcInterceptor) {
        SimpleRabbitListenerContainerFactory factory = createBasicFactory(connectionFactory, converter, amqpMdcInterceptor);
        return configureSizing(factory,
                rabbitMQListenerFactoryProperties.getMedium().getConcurrentConsumers(),
                rabbitMQListenerFactoryProperties.getMedium().getMaxConcurrentConsumers(),
                rabbitMQListenerFactoryProperties.getMedium().getPreFetchCount());
    }

    @Bean
    public SimpleRabbitListenerContainerFactory bulkPriorityListenerFactory(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter converter,
            MethodInterceptor amqpMdcInterceptor) {
        SimpleRabbitListenerContainerFactory factory = createBasicFactory(connectionFactory, converter, amqpMdcInterceptor);
        return configureSizing(factory,
                rabbitMQListenerFactoryProperties.getLow().getConcurrentConsumers(),
                rabbitMQListenerFactoryProperties.getLow().getMaxConcurrentConsumers(),
                rabbitMQListenerFactoryProperties.getLow().getPreFetchCount());
    }

    private SimpleRabbitListenerContainerFactory createBasicFactory(
            ConnectionFactory connectionFactory,
            JacksonJsonMessageConverter converter,
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