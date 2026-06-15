package com.loan_org.notification_service.domain.delivery.channels.impl;

import com.loan_org.notification_service.domain.delivery.channels.NotificationChannel;
import com.loan_org.notification_service.dto.NotificationRequest;
import com.loan_org.notification_service.dto.RenderedEmail;
import com.loan_org.notification_service.domain.template.NotificationTemplateRenderingService;
import com.loan_org.notification_service.domain.delivery.channels.NotificationChannelHandler;
import com.loan_org.notification_service.shared.exception.channel.EmailDeliveryFailureException;
import com.loan_org.notification_service.shared.util.MaskingUtil;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationChannelHandler implements NotificationChannelHandler {

    private final JavaMailSender mailSender;
    private final NotificationTemplateRenderingService renderingService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public String dispatch(NotificationRequest request) {
        log.info("Starting email dispatch service via SMTP. Recipient: {}",
                MaskingUtil.maskRecipient(request.getRecipient(), request.getChannel()));

        try {
            MimeMessage mimeMessage  = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(request.getRecipient());

            RenderedEmail emailElements = renderingService.generateHtmlMessage(request.getTemplateCode(), request.getTemplateVariables());
            helper.setSubject(emailElements.subject());
            helper.setFrom(fromEmail);
            helper.setText(emailElements.body(), true);

            mailSender.send(mimeMessage);

            String providerReferenceId = mimeMessage.getMessageID() != null
                    ? mimeMessage.getMessageID()
                    : "smtp-msg-" + java.util.UUID.randomUUID().toString().substring(0, 8);

            log.info("Email has been sent successfully. Assigned Tracking ID: {}", providerReferenceId);
            return providerReferenceId;
        } catch (Exception e) {
            log.error("Failure in delivery of email via SMTP transport. Reason: {}", e.getMessage());
            throw new EmailDeliveryFailureException(request.getRecipient(), e);
        }
    }

    @Override
    public String getSupportedChannel() {
        return NotificationChannel.EMAIL.toString();
    }
}
