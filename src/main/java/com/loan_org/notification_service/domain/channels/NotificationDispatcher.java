package com.loan_org.notification_service.domain.channels;

import com.loan_org.notification_service.dto.NotificationRequest;

public interface NotificationDispatcher {
    String routeAndDispatch(NotificationRequest request);
}
