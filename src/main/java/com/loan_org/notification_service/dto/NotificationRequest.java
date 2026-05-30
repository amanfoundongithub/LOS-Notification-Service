package com.loan_org.notification_service.dto;

import com.loan_org.notification_service.domain.NotificationChannel;
import com.loan_org.notification_service.domain.NotificationPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private String userId;

    @NotBlank(message = "Trace ID is required for logging visibility")
    private String traceId;

    @NotBlank(message = "Transaction ID is required for idempotency protection")
    private String transactionId;

    @NotBlank(message = "Recipient contact information is required")
    private String recipient;

    @NotNull(message = "Notification channel must be specified")
    private NotificationChannel channel;

    @NotBlank(message = "Template code is required to render content")
    private String templateCode;

    @NotNull(message = "Priority level must be explicitly defined")
    private NotificationPriority priority;

    private String title;
    private String fallbackContent;

    private Map<String, Object> templateVariables;
}