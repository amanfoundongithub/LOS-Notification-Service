package com.loan_org.notification_service.domain.audit.impl;

import com.loan_org.notification_service.shared.model.NotificationStatus;
import com.loan_org.notification_service.domain.audit.NotificationLogDocument;
import com.loan_org.notification_service.domain.audit.NotificationLogRepository;
import com.loan_org.notification_service.domain.audit.NotificationLogService;
import com.loan_org.notification_service.dto.NotificationRequest;
import com.loan_org.notification_service.shared.exception.mongo.RecordNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
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

    // FIX: Changed return type from void to NotificationLogDocument
    @Override
    public NotificationLogDocument updateRetryTelemetry(String id, int currentRetryCount, long upcomingDelayMs) {
        return logRepository.findById(id)
                .map(record -> {
                    record.setRetryCount(currentRetryCount);
                    record.setNextRetryAt(Instant.now().plusMillis(upcomingDelayMs));
                    record.setStatus(NotificationStatus.PENDING);
                    NotificationLogDocument updatedRecord = logRepository.save(record);
                    log.debug("[AUDIT][TELEMETRY] Incremented retry metrics count to {} for MongoID: {}", currentRetryCount, id);
                    return updatedRecord;
                })
                .orElseThrow(() -> new RecordNotFoundException("notificationLogs", id));
    }

    @Override
    public void updateStatusToSent(String id, String providerRefId) {
        logRepository.findById(id).ifPresent(record -> {
            record.setStatus(NotificationStatus.DELIVERED);
            record.setSentAt(Instant.now());
            record.setProviderReferenceId(providerRefId);
            record.setNextRetryAt(null);
            logRepository.save(record);
        });
    }

    @Override
    public void updateStatusToFailed(String id, Exception exception) {
        logRepository.findById(id).ifPresent(record -> {
            record.setStatus(NotificationStatus.FAILED);
            record.setNextRetryAt(null);
            record.setErrorCode(exception.getClass().getSimpleName());
            record.setErrorMessage(exception.getMessage() != null ? exception.getMessage() : "No detailed diagnostic payload provided.");
            logRepository.save(record);
        });
    }
}