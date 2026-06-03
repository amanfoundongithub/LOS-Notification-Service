package com.loan_org.notification_service.shared.exception.channel;

import com.loan_org.notification_service.shared.exception.NotificationDeliveryException;

public class ChannelNotSupportedException extends NotificationDeliveryException {
    public ChannelNotSupportedException(String channel) {
        super(
                "Channel: "  + channel + " is not supported in the current delivery schema. Please send " +
                        "a different channel for the message delivery."
        );
    }
}
