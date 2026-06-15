package com.loan_org.notification_service.domain.template.dto;

public record RenderedEmail(
        String subject,
        String body
) {}