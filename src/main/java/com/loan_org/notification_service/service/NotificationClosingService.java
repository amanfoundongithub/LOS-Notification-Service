package com.loan_org.notification_service.service;

import com.loan_org.notification_service.dto.NotificationRequest;

public interface NotificationClosingService {
    void complete(NotificationRequest request);
}
