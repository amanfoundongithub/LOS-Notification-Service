package com.loan_org.notification_service.config.mvc;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
@Slf4j
public class MdcInterceptor implements HandlerInterceptor {

    @Value("${mdc.traceId}")
    private String TRACE_ID_KEY;

    @Value("${mdc.traceHeader}")
    private String TRACE_HEADER;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = request.getHeader(TRACE_HEADER);
        if(traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
            log.warn("[MDC][WARNING] No traceId found for this request. The request will be processed with " +
                    "the following traceId: {}. This traceId can be found in the header: {}." +
                            "However, keep one for handy reference for request.",
                    traceId,
                    TRACE_HEADER);
        }
        MDC.put(TRACE_ID_KEY, traceId);
        response.setHeader(TRACE_HEADER, traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        MDC.clear();
    }
}
