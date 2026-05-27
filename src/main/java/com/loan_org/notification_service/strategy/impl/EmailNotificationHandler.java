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
            // 1. Instantiate an empty MIME standard message wrapper
            MimeMessage mimeMessage = mailSender.createMimeMessage();

            // 2. Use MimeMessageHelper to safely configure encoding and content structures
            // 'true' flag indicates this message framework supports multipart elements (like attachments or embedded HTML)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(logEntry.getRecipient());
            helper.setSubject(logEntry.getTitle() != null ? logEntry.getTitle() : "Account Update - LoanOrg");
            helper.setFrom("loanmanagementsystemadmin@gmail.com");

            // 3. Inject fully compiled HTML template markup text natively
            // Setting the second parameter 'true' explicitly forces the client to parse it as live HTML code instead of plaintext
            helper.setText(logEntry.getContent(), true);

            // 4. Stream payload out over the network connection wire
            mailSender.send(mimeMessage);

            // 5. Extract SMTP runtime identifier tokens or fall back onto a localized unique generation frame
            String providerReferenceId = mimeMessage.getMessageID() != null
                    ? mimeMessage.getMessageID()
                    : "smtp-msg-" + java.util.UUID.randomUUID().toString().substring(0, 8);

            log.info("Email successfully accepted by external SMTP relay node. Assigned Tracking ID: {}", providerReferenceId);

            // 6. Update document log status to DELIVERED inside MongoDB
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
