package com.loan_org.notification_service.template_service.access_control;

import java.util.Map;

public interface TemplateServiceAccessControl {
    void canCreate(Map<String, Object> attributes);
    void canRead(Map<String, Object> attributes);
    void canUpdate(Map<String, Object> attributes);
    void canDelete(Map<String, Object> attributes);
}
