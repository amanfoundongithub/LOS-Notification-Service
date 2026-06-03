package com.loan_org.notification_service.shared.exception.template;

public class TemplateCompilationFailedException extends RuntimeException {
    public TemplateCompilationFailedException(String templateCode, Throwable e) {
        super(
                "Compilation sequence failed for templateCode: " + templateCode
                        + ". Reason: " + e.getMessage()
        );
    }
}
