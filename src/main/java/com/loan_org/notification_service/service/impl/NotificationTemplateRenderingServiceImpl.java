package com.loan_org.notification_service.service.impl;

import com.loan_org.notification_service.document.NotificationTemplateDocument;
import com.loan_org.notification_service.dto.RenderedEmail;
import com.loan_org.notification_service.repository.NotificationTemplateRepository;
import com.loan_org.notification_service.service.NotificationTemplateRenderingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.StringTemplateResolver;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotificationTemplateRenderingServiceImpl implements NotificationTemplateRenderingService {

    private final NotificationTemplateRepository templateRepository;
    private final SpringTemplateEngine mongoTemplateEngine;

    public RenderedEmail generateHtmlMessage(String templateCode, Map<String, Object> variables) {
        NotificationTemplateDocument template = templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> new RuntimeException("Template not found for code: " + templateCode));

        StringTemplateResolver stringResolver = new StringTemplateResolver();
        this.mongoTemplateEngine.setTemplateResolver(stringResolver);
        Context context = new Context();
        context.setVariables(variables);
        String htmlBody = mongoTemplateEngine.process(template.getHtmlContent(), context);
        String finalSubject = mongoTemplateEngine.process(template.getSubjectLine(), context);
        return new RenderedEmail(finalSubject, htmlBody);
    }

}
