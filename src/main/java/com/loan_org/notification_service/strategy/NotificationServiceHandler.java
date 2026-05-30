package com.loan_org.notification_service.strategy;

import com.loan_org.notification_service.dto.NotificationRequest;

public interface NotificationServiceHandler {

    void dispatch(NotificationRequest logEntry);

    String getSupportedChannels();
}

