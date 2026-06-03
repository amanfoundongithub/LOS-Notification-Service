package com.loan_org.notification_service.shared.exception.channel;

import com.loan_org.notification_service.shared.exception.NotificationDeliveryException;
import com.loan_org.notification_service.shared.util.MaskingUtil;

public class EmailDeliveryFailureException extends NotificationDeliveryException {
    public EmailDeliveryFailureException(String email, Throwable e) {
        super(
                "Failed to deliver email to " + MaskingUtil.maskEmail(email) +
                        ". Reason: " + e.getMessage()
        );
    }
}
