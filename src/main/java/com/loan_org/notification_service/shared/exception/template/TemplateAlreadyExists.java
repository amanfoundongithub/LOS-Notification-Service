package com.loan_org.notification_service.shared.exception.template;

public class TemplateAlreadyExists extends RuntimeException {
    public TemplateAlreadyExists(String message) {
        super(message);
    }
}
