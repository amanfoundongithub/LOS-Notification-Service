package com.loan_org.notification_service.domain.dispatcher.channels;

import com.loan_org.notification_service.delivery_service.dto.NotificationDeliveryRequest;

/**
 * Strategy contract defining self-contained execution operations for a channel.
 *
 * @author amanfoundongithub
 * @version 1.0.0
 */
public interface NotificationChannelHandler {

    /**
     * Ingests the core request payload, resolves its internal assets, and delivers it.
     */
    String dispatch(NotificationDeliveryRequest request);

    /**
     * Identifies the unique channel type this strategy handles.
     */
    String getSupportedChannel();
}