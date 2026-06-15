package com.loan_org.notification_service.domain.delivery.impl;

import com.loan_org.notification_service.domain.delivery.channels.NotificationChannelHandler;
import com.loan_org.notification_service.domain.delivery.NotificationDispatcher;
import com.loan_org.notification_service.delivery_service.dto.NotificationDeliveryRequest;
import com.loan_org.notification_service.shared.exception.channel.ChannelNotPresentException;
import com.loan_org.notification_service.shared.exception.channel.ChannelNotSupportedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NotificationDispatcherImpl implements NotificationDispatcher {

    private final Map<String, NotificationChannelHandler> strategyMap;

    public NotificationDispatcherImpl(List<NotificationChannelHandler> handlers) {
        this.strategyMap = handlers.stream()
                .filter(h -> Objects.nonNull(h.getSupportedChannel()))
                .collect(
                Collectors.toMap(
                        handler -> handler.getSupportedChannel().toUpperCase(),
                        Function.identity()
                )
        );
        log.info("[DISPATCHER][INIT] Successful Initialization of Dispatching Engine with supported channel(s): {}", strategyMap.keySet());
    }

    /**
     * Routes the fully compiled message to its matching infrastructure gateway provider.
     */
    public String dispatch(NotificationDeliveryRequest request) {
        if(request.getChannel() == null) {
            log.error("[DISPATCHER][ERROR] Inbound delivery payload missing target channel token.");
            throw new ChannelNotPresentException();
        }
        String channel = request.getChannel().toString().toUpperCase();
        NotificationChannelHandler handler = strategyMap.get(channel);

        if (handler == null) {
            log.error("[DISPATCHER][ERROR] Unsupported channel configuration format: '{}'", channel);
            throw new ChannelNotSupportedException(channel);
        }

        return handler.dispatch(request);
    }
}