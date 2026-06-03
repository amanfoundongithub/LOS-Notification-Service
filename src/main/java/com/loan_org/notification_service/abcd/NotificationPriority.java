package com.loan_org.notification_service.abcd;

import java.util.Arrays;

/**
 * Defines the classification tiers used to prioritize message execution speeds
 * and routing urgencies across dispatch consumers.
 *
 * @author amanfoundongithub
 * @version 1.0.0
 */
public enum NotificationPriority {
    HIGH,
    MEDIUM,
    LOW;

    /**
     * Converts the priority variant name into a standardized lowercase string,
     * allowing for dynamic routing key segment generation.
     *
     * @return lowercase string representation of the priority (e.g., "high")
     */
    public String toRoutingPriorityPart() {
        return this.name().toLowerCase();
    }

    /**
     * Safely maps an incoming string token into its equivalent NotificationPriority variant.
     *
     * @param value The string payload to process
     * @return The matching NotificationPriority, or MEDIUM as a safe default fallback
     */
    public static NotificationPriority fromString(String value) {
        if(value == null) {
            return null;
        }
        return Arrays.stream(NotificationPriority.values())
                .filter(channel -> channel.name().equalsIgnoreCase(value))
                .findFirst()
                .orElse(null);
    }
}