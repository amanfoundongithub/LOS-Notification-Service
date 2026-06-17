package com.loan_org.notification_service.template_service.access_control;

import java.util.Map;

public interface TemplateServiceAccessControl {
    void canCreate(Map<String, Object> attributes);
}
