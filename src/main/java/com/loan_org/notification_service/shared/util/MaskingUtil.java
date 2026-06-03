package com.loan_org.notification_service.shared.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UtilityClass
public class MaskingUtil {

    public static String maskEmail(String email) {
        if (email == null || email.isBlank() || !email.contains("@")) {
            return "********";
        }
        try {
            int parts = email.indexOf("@");
            String localPart = email.substring(0, parts);
            String domainPart = email.substring(parts);

            if (localPart.length() <= 2) {
                return localPart.charAt(0) + "***" + domainPart;
            }

            return localPart.charAt(0)
                    + "*".repeat(localPart.length() - 2)
                    + localPart.charAt(localPart.length() - 1)
                    + domainPart;
        } catch (Exception e) {
            return "********";
        }
    }

    public static String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "********";
        }
        String cleanPhone = phone.trim();
        if (cleanPhone.length() <= 4) {
            return "****";
        }
        return "*".repeat(cleanPhone.length() - 4) + cleanPhone.substring(cleanPhone.length() - 4);
    }

}
