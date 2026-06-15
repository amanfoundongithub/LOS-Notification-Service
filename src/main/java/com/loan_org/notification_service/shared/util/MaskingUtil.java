package com.loan_org.notification_service.shared.util;

import com.loan_org.notification_service.domain.channels.NotificationChannel;

public class MaskingUtil {

    public static final String NO_RECIPIENT_PROVIDED = "*****";

    private MaskingUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated.");
    }

    public static String maskRecipient(String recipient, NotificationChannel channel) {
        if(recipient == null || recipient.isBlank()) {
            return NO_RECIPIENT_PROVIDED;
        }
        if(channel == NotificationChannel.EMAIL) {
            return maskEmail(recipient);
        } else if(channel == NotificationChannel.SMS) {
            return maskPhoneNumber(recipient);
        }
        return NO_RECIPIENT_PROVIDED;
    }

    private static String maskEmail(String email) {
        if(!email.contains("@")) {
            return NO_RECIPIENT_PROVIDED;
        }
        try {
            int parts = email.indexOf("@");
            String localPart  = email.substring(0, parts);
            String domainPart = email.substring(parts);
            if (localPart.length() <= 2) {
                return localPart.charAt(0) + "***" + domainPart;
            }
            return localPart.charAt(0)
                    + "*".repeat(localPart.length() - 2)
                    + localPart.charAt(localPart.length() - 1)
                    + domainPart;

        } catch (Exception _) {
            return NO_RECIPIENT_PROVIDED;
        }
    }

    private static String maskPhoneNumber(String phoneNumber) {
        String cleanPhone = phoneNumber.trim();
        if(cleanPhone.length() <= 4) {
            return NO_RECIPIENT_PROVIDED;
        }
        return "*".repeat(cleanPhone.length() - 4) + cleanPhone.substring(cleanPhone.length() - 4);
    }

}
