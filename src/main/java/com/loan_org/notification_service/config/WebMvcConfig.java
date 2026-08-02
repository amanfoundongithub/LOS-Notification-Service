package com.loan_org.notification_service.config;

import com.loan_org.notification_service.config.interceptor.JwtInterceptor;
import com.loan_org.notification_service.config.interceptor.MdcInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final MdcInterceptor mdcInterceptor;
    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mdcInterceptor)
                .addPathPatterns("/api/**");
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**") // Applies to your API routes
                .allowedOrigins("http://localhost:3000") // Whitelists your frontend origin
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH") // Includes preflight OPTIONS method
                .allowedHeaders("*") // Permits all incoming request headers
                .allowCredentials(true); // Allows cookies or Authorization headers if needed
    }
}
