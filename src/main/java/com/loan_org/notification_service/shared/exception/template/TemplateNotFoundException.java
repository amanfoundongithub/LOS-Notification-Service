package com.loan_org.notification_service.shared.exception.template;

public class TemplateNotFoundException extends RuntimeException {
    public TemplateNotFoundException(String templateCode) {
        super(
                "Notification Template not found for templateCode" + templateCode +". Please consider passing" +
                        "the correct template code."
        );
    }
}
