package com.loan_org.notification_service.shared.exception;

public class NotificationDeliveryException extends RuntimeException {
    public NotificationDeliveryException(String message) {
        super(message);
    }
}
