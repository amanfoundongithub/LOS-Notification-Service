package com.loan_org.notification_service.shared.util;

import com.loan_org.notification_service.config.RabbitMQConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
@Slf4j
public class MdcInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = request.getHeader(RabbitMQConfig.RABBITMQ_MDC_HEADER);
        if(traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
            log.warn("[MDC][WARNING] No traceId found for this request. The request will be processed with " +
                            "the following traceId: {}. This traceId can be found in the header: {}." +
                            "However, keep one for handy reference for request.",
                    traceId,
                    RabbitMQConfig.RABBITMQ_MDC_HEADER);
        }
        MDC.put(RabbitMQConfig.RABBITMQ_MDC_KEY, traceId);
        response.setHeader(RabbitMQConfig.RABBITMQ_MDC_HEADER, traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.clear();
    }
}
