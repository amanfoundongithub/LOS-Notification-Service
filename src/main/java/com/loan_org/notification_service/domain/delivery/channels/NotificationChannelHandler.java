package com.loan_org.notification_service.domain.delivery.channels;

import com.loan_org.notification_service.dto.NotificationRequest;

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
    String dispatch(NotificationRequest request);

    /**
     * Identifies the unique channel type this strategy handles.
     */
    String getSupportedChannel();
}