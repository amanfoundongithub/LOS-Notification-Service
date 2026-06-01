package com.loan_org.notification_service.config.amqp;

import com.loan_org.notification_service.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class RabbitMdcConfig {

    private final RabbitMdcHandshakeProcessor rabbitMdcHandshakeProcessor;

    @Bean
    public Jackson2JsonMessageConverter consumerJackson2MessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();

        // Configure a class mapper to handle differing package names between microservices
        DefaultClassMapper classMapper = new DefaultClassMapper();

        // Explicitly trust everything so packages don't cause deserialization blocks
        classMapper.setTrustedPackages("*");

        // Map the sender's __TypeId__ token straight to your local target class payload
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("com.loan_org.identity_and_access_management.dto.NotificationEventDto", NotificationRequest.class);
        classMapper.setIdClassMapping(idClassMapping);

        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory, Jackson2JsonMessageConverter consumerJackson2MessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);

        factory.setAfterReceivePostProcessors(rabbitMdcHandshakeProcessor);
        factory.setMessageConverter(consumerJackson2MessageConverter);
        return factory;
    }

}
