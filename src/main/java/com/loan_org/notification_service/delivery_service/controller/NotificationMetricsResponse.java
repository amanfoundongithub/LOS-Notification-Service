package com.loan_org.notification_service.delivery_service.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMetricsResponse {
    private long totalProcessed;
    private long totalSent;
    private long totalFailed;
    private long queueBacklog;
    private long pendingCount;
    private double failurePercentage;
    private double successPercentage;
}