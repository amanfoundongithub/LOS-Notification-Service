package com.loan_org.notification_service.domain;

import java.util.Arrays;

/**
 * Defines the supported physical distribution pathways for outgoing dispatches.
 * Includes architectural utilities for AMQP routing infrastructure.
 *
 * @author amanfoundongithub
 */
public enum NotificationChannel {
    EMAIL,
    SMS,
    PUSH;

    /**
     * Converts the enum name into a standardized lowercase string suitable
     * for constructing dynamic AMQP topic routing key patterns.
     *
     * @return lowercase string representation of the channel (e.g., "email")
     */
    public String toRoutingKeyPart() {
        return this.name().toLowerCase();
    }

    /**
     * Safely parses an incoming string value into its corresponding enum variant.
     * Prevents system-level termination on invalid inputs by returning null or an explicit fallback.
     *
     * @param value The raw string to parse
     * @return The matching NotificationChannel, or null if unsupported
     */
    public static NotificationChannel fromString(String value) {
        if(value == null) {
            return null;
        }
        return Arrays.stream(NotificationChannel.values())
                .filter(channel -> channel.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}