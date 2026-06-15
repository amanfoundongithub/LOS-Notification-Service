package com.loan_org.notification_service.template_service.dto;

import com.loan_org.notification_service.shared.validation.ValidHtml;
import jakarta.validation.constraints.NotBlank;

public record TemplateCreationRequest(

        @NotBlank(message = "Template code is required to identify template")
        String templateCode,

        @NotBlank(message = "Subject Line is needed for template")
        String subjectLine,

        @NotBlank(message = "HTML Content is needed for template")
        @ValidHtml(message = "Invalid HTML Content detected for template")
        String htmlContent

){}