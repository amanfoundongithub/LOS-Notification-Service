package com.loan_org.notification_service.delivery_service.service;

import com.loan_org.notification_service.delivery_service.dto.NotificationDeliveryRequest;

public interface NotificationDeliveryService {
    void execute(NotificationDeliveryRequest request);
}
