package com.loan_org.notification_service.domain;

public enum NotificationChannel {
    EMAIL,
    SMS,
    PUSH;

    public String toRoutingKeyPart() {
        return this.name().toLowerCase();
    }
}