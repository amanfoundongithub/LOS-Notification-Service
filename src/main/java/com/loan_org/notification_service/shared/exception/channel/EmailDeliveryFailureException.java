package com.loan_org.notification_service.shared.exception.channel;

import com.loan_org.notification_service.domain.dispatcher.channels.NotificationChannel;
import com.loan_org.notification_service.shared.exception.NotificationDeliveryException;

import static com.loan_org.notification_service.shared.util.MaskingUtil.maskRecipient;

public class EmailDeliveryFailureException extends NotificationDeliveryException {
    public EmailDeliveryFailureException(String email, Throwable e) {
        super(
                "Failed to deliver email to " + maskRecipient(email, NotificationChannel.EMAIL) +
                        ". Reason: " + e.getMessage()
        );
    }
}
