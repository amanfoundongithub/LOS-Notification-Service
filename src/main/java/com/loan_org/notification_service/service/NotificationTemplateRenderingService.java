package com.loan_org.notification_service.service;

import com.loan_org.notification_service.document.NotificationTemplateDocument;
import com.loan_org.notification_service.dto.RenderedEmail;
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

    public RenderedEmail generateHtmlMessage(String templateCode, Map<String, Object> variables) {
        NotificationTemplateDocument template = templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> new RuntimeException("Template not found for code: " + templateCode));

        Context context = new Context();
        context.setVariables(variables);
        String htmlBody = mongoTemplateEngine.process(template.getHtmlContent(), context);
        String finalSubject = mongoTemplateEngine.process(template.getSubjectLine(), context);
        return new RenderedEmail(finalSubject, htmlBody);
    }

}
