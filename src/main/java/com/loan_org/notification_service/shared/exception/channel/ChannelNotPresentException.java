package com.loan_org.notification_service.shared.exception.channel;

import com.loan_org.notification_service.shared.exception.NotificationDeliveryException;

public class ChannelNotPresentException extends NotificationDeliveryException {
    public ChannelNotPresentException() {
        super(
                "No channel was present in the request. Please provide one to ensure delivery."
        );
    }
}
