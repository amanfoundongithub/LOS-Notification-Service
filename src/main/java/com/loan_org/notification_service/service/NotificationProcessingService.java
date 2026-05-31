package com.loan_org.notification_service.service;

import com.loan_org.notification_service.dto.NotificationRequest;

public interface NotificationProcessingService {
    void process(NotificationRequest request);
}
