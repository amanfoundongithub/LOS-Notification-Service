package com.loan_org.notification_service.strategy;

import com.loan_org.notification_service.document.NotificationLogDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class NotificationDispatcher {

    private final Map<String, NotificationServiceHandler> strategyMap;

    // Spring autowires all available NotificationHandler components straight into this constructor
    public NotificationDispatcher(List<NotificationServiceHandler> handlers) {
        this.strategyMap = handlers.stream().collect(
                Collectors.toMap(
                        handler -> handler.getSupportedChannels().toUpperCase(),
                        Function.identity()
                )
        );
        log.info("Notification Dispatcher successfully initialized with channels: {}", strategyMap.keySet());
    }

    /**
     * Routes the fully compiled message to its matching infrastructure gateway provider.
     */
    public void routeAndDispatch(NotificationLogDocument logEntry) {
        String channel = logEntry.getChannel().toString().toUpperCase();
        NotificationServiceHandler handler = strategyMap.get(channel);

        if (handler == null) {
            log.error("Unsupported channel configuration format: '{}' for log ID: {}", channel, logEntry.getId());
            throw new IllegalArgumentException("No concrete provider strategy registered for channel: " + channel);
        }

        handler.dispatch(logEntry);
    }
}