package com.loan_org.notification_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.StringTemplateResolver;

@Configuration
public class ThymeLeafMongoConfig {

    public static final String MONGO_TEMPLATE_ENGINE_BEAN = "mongoTemplateEngine";
    public static final long CACHE_LIFE_IN_MS = 3600000L;

    @Bean(name = MONGO_TEMPLATE_ENGINE_BEAN)
    public SpringTemplateEngine mongoTemplateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();

        // String resolver configuration
        StringTemplateResolver stringResolver = new StringTemplateResolver();
        stringResolver.setCacheable(true);
        stringResolver.setCacheTTLMs(CACHE_LIFE_IN_MS);

        // Bind the engine to the template resolver
        engine.setTemplateResolver(stringResolver);
        return engine;
    }

}
