package com.loan_org.notification_service.config;

import com.loan_org.notification_service.config.interceptor.JwtInterceptor;
import com.loan_org.notification_service.config.interceptor.MdcInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
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

}
