package com.loan_org.notification_service.domain.audit.service;

import com.loan_org.notification_service.domain.audit.entity.NotificationLogDocument;
import com.loan_org.notification_service.delivery_service.dto.NotificationDeliveryRequest;

public interface NotificationLogService {
    NotificationLogDocument createRecord(NotificationDeliveryRequest request);
    NotificationLogDocument getRecordById(String id);
    NotificationLogDocument updateRetryTelemetry(String id, int currentRetryCount, long upcomingDelayMs);
    void                    updateStatusToSent(String id, String providerRefId);
    void                    updateStatusToFailed(String id, Exception exception);
}
