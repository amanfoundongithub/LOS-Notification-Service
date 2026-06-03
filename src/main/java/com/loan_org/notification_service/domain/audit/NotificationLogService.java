package com.loan_org.notification_service.domain.audit;

import com.loan_org.notification_service.dto.NotificationRequest;

public interface NotificationLogService {
    NotificationLogDocument createRecord(NotificationRequest request);
    NotificationLogDocument getRecordById(String id);
    void updateRetryTelemetry(String id, int currentRetryCount, long upcomingDelayMs);
    void updateStatusToSent(String id, String providerRefId);
    public void updateStatusToFailed(String id, Exception exception);
}
