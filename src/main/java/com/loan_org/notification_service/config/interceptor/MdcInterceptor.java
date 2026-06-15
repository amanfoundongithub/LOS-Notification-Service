package com.loan_org.notification_service.config.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
@Slf4j
public class MdcInterceptor implements HandlerInterceptor {

    @Value("${interceptor.mdc.trace.key}")
    private String traceKey;

    @Value("${interceptor.mdc.trace.header}")
    private String traceHeader;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) {

        String traceId = request.getHeader(traceHeader);
        if(traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
            log.warn("WARNING: Missing request trace header [{}]. Defaulting to a random fallback: {}",
                    traceHeader,
                    traceId);
        }
        MDC.put(traceKey, traceId);
        response.setHeader(traceHeader, traceId);
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request,
                                @NonNull HttpServletResponse response,
                                @NonNull Object handler, Exception ex) {
        MDC.clear();
    }
}
