package com.loan_org.notification_service.dto;

public record RenderedEmail(
        String subject,
        String body
) {}