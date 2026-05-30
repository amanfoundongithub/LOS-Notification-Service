package com.loan_org.notification_service.service;

import com.loan_org.notification_service.document.NotificationTemplateDocument;
import com.loan_org.notification_service.repository.NotificationTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationTemplateRenderingService {

    private final NotificationTemplateRepository templateRepository;
    private final TemplateEngine mongoTemplateEngine;

    public String generateEmailHtml(String templateCode, Map<String, Object> variables) {
        NotificationTemplateDocument template = templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> new RuntimeException("Template not found for code: " + templateCode));

        Context context = new Context();
        context.setVariables(variables);
        return mongoTemplateEngine.process(template.getHtmlContent(), context);
    }

}
