package com.loan_org.notification_service.service;

import com.loan_org.notification_service.dto.NotificationRequest;

public interface NotificationInitiatorService {
    void start(NotificationRequest request);
}
