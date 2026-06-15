package com.loan_org.notification_service.domain.audit.service.impl;

import com.loan_org.notification_service.shared.model.NotificationStatus;
import com.loan_org.notification_service.domain.audit.entity.NotificationLogDocument;
import com.loan_org.notification_service.domain.audit.repository.NotificationLogRepository;
import com.loan_org.notification_service.domain.audit.service.NotificationLogService;
import com.loan_org.notification_service.dto.NotificationRequest;
import com.loan_org.notification_service.shared.exception.mongo.RecordNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationLogServiceImpl implements NotificationLogService {

    private final NotificationLogRepository logRepository;

    @Override
    public NotificationLogDocument createRecord(NotificationRequest request) {
        NotificationLogDocument logEntry = NotificationLogDocument.builder()
                .userId(request.getUserId())
                .transactionId(request.getTransactionId())
                .recipient(request.getRecipient())
                .channel(request.getChannel())
                .templateCode(request.getTemplateCode())
                .priority(request.getPriority())
                .title(request.getTitle())
                .templateVariables(request.getTemplateVariables())
                .status(NotificationStatus.PENDING)
                .build();
        return logRepository.save(logEntry);
    }

    @Override
    public NotificationLogDocument getRecordById(String id) {
        return logRepository.findById(id)
                .orElseThrow(() -> new RecordNotFoundException("notificationLogs", id));
    }

    @Override
    public NotificationLogDocument updateRetryTelemetry(String id, int currentRetryCount, long upcomingDelayMs) {
        return logRepository.findById(id)
                .map(notificationLog -> {
                    notificationLog.setRetryCount(currentRetryCount);
                    notificationLog.setNextRetryAt(Instant.now().plusMillis(upcomingDelayMs));
                    notificationLog.setStatus(NotificationStatus.PENDING);
                    NotificationLogDocument updatedRecord = logRepository.save(notificationLog);
                    log.debug("Retry updated to: {} in MongoDB with id: {}", currentRetryCount, id);
                    return updatedRecord;
                })
                .orElseThrow(() -> new RecordNotFoundException("notificationLogs", id));
    }

    @Override
    public void updateStatusToSent(String id, String providerRefId) {
        logRepository.findById(id).ifPresent(notificationLog -> {
            notificationLog.setStatus(NotificationStatus.DELIVERED);
            notificationLog.setSentAt(Instant.now());
            notificationLog.setProviderReferenceId(providerRefId);
            notificationLog.setNextRetryAt(null);
            logRepository.save(notificationLog);
        });
    }

    @Override
    public void updateStatusToFailed(String id, Exception exception) {
        logRepository.findById(id).ifPresent(notificationLog -> {
            notificationLog.setStatus(NotificationStatus.FAILED);
            notificationLog.setNextRetryAt(null);
            notificationLog.setErrorCode(exception.getClass().getSimpleName());
            notificationLog.setErrorMessage(exception.getMessage() != null ? exception.getMessage() : "No detailed diagnostic payload provided.");
            logRepository.save(notificationLog);
        });
    }
}