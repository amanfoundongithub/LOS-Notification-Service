package com.loan_org.notification_service.config.amqp;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class RabbitMdcHandshakeProcessor implements MessagePostProcessor {

    @Value("${mdc.traceId}")
    private String TRACE_ID_KEY;

    @Override
    public Message postProcessMessage(Message message) {
        Object traceIdHeader = message.getMessageProperties().getHeaders().get(TRACE_ID_KEY);
        String traceId;
        if(traceIdHeader != null) {
            traceId = traceIdHeader.toString();
        } else {
            traceId = UUID.randomUUID().toString();
            log.warn("[MDC][WARNING] No traceId found for this MQ message. The message will be processed with " +
                            "the following traceId: {}.",
                    traceId);
        }
        MDC.put(TRACE_ID_KEY, traceId);
        return message;
    }

    public static void clearMdc() {
        MDC.clear();
    }

}
