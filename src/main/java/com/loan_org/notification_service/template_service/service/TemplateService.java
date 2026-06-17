package com.loan_org.notification_service.template_service.service;

import com.loan_org.notification_service.domain.template.entity.NotificationTemplateDocument;
import com.loan_org.notification_service.template_service.dto.TemplateCreationRequest;
import com.loan_org.notification_service.template_service.dto.TemplateUpdateRequest;

public interface TemplateService {
    NotificationTemplateDocument createTemplate(TemplateCreationRequest request);
    NotificationTemplateDocument updateTemplate(TemplateUpdateRequest request);
    NotificationTemplateDocument getTemplate(String templateCode);
    void                         deleteTemplate(String templateCode);
}
