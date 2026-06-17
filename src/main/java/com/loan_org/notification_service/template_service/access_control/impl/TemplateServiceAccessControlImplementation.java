package com.loan_org.notification_service.template_service.access_control.impl;

import com.loan_org.notification_service.shared.exception.AccessForbiddenException;
import com.loan_org.notification_service.template_service.access_control.TemplateServiceAccessControl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class TemplateServiceAccessControlImplementation implements TemplateServiceAccessControl {

    private static final String FORBIDDEN_ACCESS_MESSAGE =
            "The given user does not have permission for %s. Please ensure that you have the desired permission " +
                    "to perform this operation. Otherwise, contact the administrator";

    @Override
    public void canCreate(Map<String, Object> attributes) {
        keyExists(attributes, "notificationTemplate:create");
    }

    @Override
    public void canRead(Map<String, Object> attributes) {
        keyExists(attributes, "notificationTemplate:read");
    }

    @Override
    public void canUpdate(Map<String, Object> attributes) {
        keyExists(attributes, "notificationTemplate:update");
    }

    @Override
    public void canDelete(Map<String, Object> attributes) {
        keyExists(attributes, "notificationTemplate:delete");
    }


    private void keyExists(Map<String, Object> attributes, String key) {
        if(!attributes.containsKey(key)) {
            throw new AccessForbiddenException(
                String.format(FORBIDDEN_ACCESS_MESSAGE, key)
            );
        }
    }
}
