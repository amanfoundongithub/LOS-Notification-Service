package com.loan_org.notification_service.domain.template.service.impl;

import com.loan_org.notification_service.config.ThymeLeafMongoConfig;
import com.loan_org.notification_service.domain.template.entity.NotificationTemplateDocument;
import com.loan_org.notification_service.domain.template.repository.NotificationTemplateRepository;
import com.loan_org.notification_service.domain.template.service.NotificationTemplateRenderingService;
import com.loan_org.notification_service.domain.template.dto.RenderedEmail;
import com.loan_org.notification_service.shared.exception.template.TemplateCompilationFailedException;
import com.loan_org.notification_service.shared.exception.template.TemplateNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Slf4j
@Service
public class NotificationTemplateRenderingServiceImpl implements NotificationTemplateRenderingService {

    private final NotificationTemplateRepository templateRepository;
    private final TemplateEngine templateEngine;

    public NotificationTemplateRenderingServiceImpl(
            NotificationTemplateRepository templateRepository,
            @Qualifier(ThymeLeafMongoConfig.MONGO_TEMPLATE_ENGINE_BEAN) SpringTemplateEngine templateEngine) {
        this.templateRepository = templateRepository;
        this.templateEngine = templateEngine;
    }

    @Override
    public RenderedEmail generateHtmlMessage(String templateCode, Map<String, Object> variables) {
        log.info("Rendering communication payload assets for templateCode: {}", templateCode);

        NotificationTemplateDocument template = templateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> new TemplateNotFoundException(templateCode));

        Context context = new Context();
        if (variables != null) {
            context.setVariables(variables);
        }

        try {
            String htmlBody = templateEngine.process(template.getHtmlContent(), context);
            String finalSubject = templateEngine.process(template.getSubjectLine(), context);
            return new RenderedEmail(finalSubject, htmlBody);
        } catch (Exception e) {
            log.error("Compilation sequence crashed for templateCode: {}", templateCode, e);
            throw new TemplateCompilationFailedException(templateCode, e);
        }
    }
}