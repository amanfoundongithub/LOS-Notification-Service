package com.loan_org.notification_service.template_service.service.impl;

import com.loan_org.notification_service.domain.template.entity.NotificationTemplateDocument;
import com.loan_org.notification_service.domain.template.repository.NotificationTemplateRepository;
import com.loan_org.notification_service.shared.exception.template.TemplateAlreadyExists;
import com.loan_org.notification_service.template_service.dto.TemplateCreationRequest;
import com.loan_org.notification_service.template_service.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private final NotificationTemplateRepository templateRepository;

    @Override
    @Transactional
    public NotificationTemplateDocument createTemplate(TemplateCreationRequest request) {

        Optional<NotificationTemplateDocument> optionalDoc = templateRepository.findByTemplateCode(request.templateCode());
        if(optionalDoc.isPresent()) {
            log.warn("Template already exists for template code {}. Aborting.", request.templateCode());
            throw new TemplateAlreadyExists("Template already exists for template code " + request.templateCode());
        }

        return templateRepository.save(
                NotificationTemplateDocument.builder()
                        .id(java.util.UUID.randomUUID().toString())
                        .templateCode(request.templateCode())
                        .subjectLine(request.subjectLine())
                        .htmlContent(request.htmlContent())
                        .build()
        );
    }




}
