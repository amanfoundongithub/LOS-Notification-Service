package com.loan_org.notification_service.template_service.dto;

import com.loan_org.notification_service.shared.validation.ValidHtml;
import jakarta.validation.constraints.NotBlank;

public record TemplateUpdateRequest(

        @NotBlank(message = "Template code is required to identify template")
        String templateCode,

        String subjectLine,

        @ValidHtml
        String htmlContent
) {}