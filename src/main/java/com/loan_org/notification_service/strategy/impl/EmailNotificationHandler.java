package com.loan_org.notification_service.strategy.impl;

import com.loan_org.notification_service.document.NotificationLogDocument;
import com.loan_org.notification_service.service.NotificationLogService;
import com.loan_org.notification_service.strategy.NotificationServiceHandler;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationHandler implements NotificationServiceHandler {

    private final NotificationLogService logService;
    private final JavaMailSender mailSender;

    @Override
    public void dispatch(NotificationLogDocument logEntry) {
        log.info("Preparing live MIME message structure for transmission. Recipient: {}", logEntry.getRecipient());

        try {
            MimeMessage mimeMessage  = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(logEntry.getRecipient());
            helper.setSubject(logEntry.getTitle() != null ? logEntry.getTitle() : "Account Update - LoanOrg");
            helper.setFrom("loanmanagementsystemadmin@gmail.com");
            helper.setText(logEntry.getContent(), true);

            mailSender.send(mimeMessage);

            String providerReferenceId = mimeMessage.getMessageID() != null
                    ? mimeMessage.getMessageID()
                    : "smtp-msg-" + java.util.UUID.randomUUID().toString().substring(0, 8);

            log.info("Email successfully accepted by external SMTP relay node. Assigned Tracking ID: {}", providerReferenceId);
            logService.markAsDelivered(logEntry.getId(), providerReferenceId);

        } catch (Exception e) {
            log.error("SMTP transport subsystem network transmission failure for log ID: {}", logEntry.getId(), e);
            // Re-throwing forces the AMQP consumer loop to fail, pushing the record automatically down your DLX/DLQ retry topology
            throw new RuntimeException("External Email delivery network pipeline failure", e);
        }
    }

    @Override
    public String getSupportedChannels() {
        return "EMAIL";
    }
}
