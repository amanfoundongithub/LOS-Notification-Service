package com.loan_org.notification_service.template_service.controller;

import com.loan_org.notification_service.domain.template.entity.NotificationTemplateDocument;
import com.loan_org.notification_service.template_service.access_control.TemplateServiceAccessControl;
import com.loan_org.notification_service.template_service.dto.TemplateCreationRequest;
import com.loan_org.notification_service.template_service.service.TemplateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.template.base_url}")
public class TemplateServiceController {

    private final TemplateService templateService;
    private final TemplateServiceAccessControl templateServiceAccessControl;

    @PostMapping("/create")
    public ResponseEntity<NotificationTemplateDocument> createTemplate(
            @Valid @RequestBody TemplateCreationRequest request,
            @RequestAttribute("attributes") Map<String, Object> attributes
    ) {
        templateServiceAccessControl.canCreate(attributes);
        return ResponseEntity.status(HttpStatus.CREATED).body(templateService.createTemplate(request));
    }

}
