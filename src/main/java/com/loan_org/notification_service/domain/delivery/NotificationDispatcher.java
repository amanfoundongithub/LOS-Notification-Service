package com.loan_org.notification_service.domain.delivery;

import com.loan_org.notification_service.delivery_service.dto.NotificationDeliveryRequest;

/**
 * Dispatcher to dispatch the request to the appropriate channel
 *
 * @author amanfoundongithub
 */
public interface NotificationDispatcher {

    /**
     * Dispatches the request to the appropriate channel
     *
     * @param request The request to be sent
     * @return The provider referenceId from the provider
     */
    String dispatch(NotificationDeliveryRequest request);
}
