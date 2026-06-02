package com.loan_org.notification_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.StringTemplateResolver;

@Configuration
public class ThymeLeafMongoConfig {

    public static final String MONGO_TEMPLATE_ENGINE_BEAN = "mongoTemplateEngine";

    @Bean(name = MONGO_TEMPLATE_ENGINE_BEAN)
    public TemplateEngine mongoTemplateEngine() {
        TemplateEngine engine = new TemplateEngine();

        // Resolver
        StringTemplateResolver stringResolver = new StringTemplateResolver();
        stringResolver.setCacheable(true);
        stringResolver.setCacheTTLMs(3600000L);

        engine.setTemplateResolver(stringResolver);
        return engine;
    }

}
