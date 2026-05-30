package com.loan_org.notification_service.domain;

public enum NotificationPriority {
    HIGH,
    MEDIUM,
    LOW;

    public String toRoutingPriorityPart() {
        return this.name().toLowerCase();
    }
}