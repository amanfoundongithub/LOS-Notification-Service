package com.loan_org.notification_service.dto;

import com.loan_org.notification_service.domain.NotificationChannel;
import com.loan_org.notification_service.domain.NotificationPriority;
import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class NotificationRequest {
    private String userId;
    private String transactionId;
    private String recipient;
    private NotificationChannel channel;
    private String templateCode;
    private NotificationPriority priority;
    private String title;
    private String fallbackContent;
    private Map<String, Object> templateVariables;
}