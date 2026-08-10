package com.loan_org.notification_service.delivery_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loan_org.notification_service.domain.audit.service.NotificationLogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/notifications/metrics")
@RequiredArgsConstructor
public class NotificationMetricsController {

    private final NotificationLogService notificationLogService;

    /**
     * Retrieves aggregated notification dispatch statistics including delivery counts,
     * failure rates, and queue backlog metrics.
     *
     * @return {@link ResponseEntity} containing {@link NotificationMetricsResponse}
     */
    @GetMapping
    public ResponseEntity<NotificationMetricsResponse> getMetrics() {
        NotificationMetricsResponse metrics = notificationLogService.getNotificationMetrics();
        return ResponseEntity.ok(metrics);
    }
}
