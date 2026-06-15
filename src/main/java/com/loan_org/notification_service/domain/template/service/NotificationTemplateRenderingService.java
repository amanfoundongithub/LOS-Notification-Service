package com.loan_org.notification_service.domain.template.service;

import com.loan_org.notification_service.dto.RenderedEmail;
import java.util.Map;

public interface NotificationTemplateRenderingService {
    RenderedEmail generateHtmlMessage(String templateCode, Map<String, Object> variables);
}
