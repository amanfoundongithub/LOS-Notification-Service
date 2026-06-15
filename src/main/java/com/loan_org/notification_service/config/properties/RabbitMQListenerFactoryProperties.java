package com.loan_org.notification_service.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "listener.factory")
public class RabbitMQListenerFactoryProperties {

    private FactorySettings high;
    private FactorySettings medium;
    private FactorySettings low;

    @Data
    public static class FactorySettings {
        private int concurrentConsumers;
        private int maxConcurrentConsumers;
        private int preFetchCount;
    }
}
