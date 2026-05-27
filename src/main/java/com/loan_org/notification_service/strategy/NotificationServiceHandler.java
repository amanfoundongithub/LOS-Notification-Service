package com.loan_org.notification_service.strategy;

import com.loan_org.notification_service.document.NotificationLogDocument;

public interface NotificationServiceHandler {

    void dispatch(NotificationLogDocument logEntry);

    String getSupportedChannels();
}

