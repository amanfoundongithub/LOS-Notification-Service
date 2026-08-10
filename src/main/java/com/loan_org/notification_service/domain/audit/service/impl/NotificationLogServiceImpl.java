package com.loan_org.notification_service.domain.audit.service.impl;

import com.loan_org.notification_service.shared.model.NotificationStatus;
import com.loan_org.notification_service.domain.audit.entity.NotificationLogDocument;
import com.loan_org.notification_service.domain.audit.repository.NotificationLogRepository;
import com.loan_org.notification_service.domain.audit.service.NotificationLogService;
import com.loan_org.notification_service.delivery_service.controller.NotificationMetricsResponse;
import com.loan_org.notification_service.delivery_service.dto.NotificationDeliveryRequest;
import com.loan_org.notification_service.shared.exception.mongo.RecordNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationLogServiceImpl implements NotificationLogService {

    private final NotificationLogRepository logRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public NotificationLogDocument createRecord(NotificationDeliveryRequest request) {
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

    @Override
public NotificationMetricsResponse getNotificationMetrics() {
    Aggregation aggregation = Aggregation.newAggregation(
        Aggregation.group()
            .count().as("totalProcessed")
            .sum(
                ConditionalOperators.when(Criteria.where("status").is(NotificationStatus.DELIVERED))
                    .then(1).otherwise(0)
            ).as("totalSent")
            .sum(
                ConditionalOperators.when(
                    Criteria.where("status").in(NotificationStatus.FAILED, NotificationStatus.ABANDONED)
                ).then(1).otherwise(0)
            ).as("totalFailed")
            .sum(
                ConditionalOperators.when(Criteria.where("status").is(NotificationStatus.ABANDONED))
                    .then(1).otherwise(0)
            ).as("queueBacklog")
            .sum(
                ConditionalOperators.when(Criteria.where("status").is(NotificationStatus.PENDING))
                    .then(1).otherwise(0)
            ).as("pendingCount")
    );

    AggregationResults<MetricsQueryResult> results = mongoTemplate.aggregate(
        aggregation,
        NotificationLogDocument.class,
        MetricsQueryResult.class
    );

    MetricsQueryResult raw = results.getUniqueMappedResult();
    if (raw == null || raw.getTotalProcessed() == 0) {
        return NotificationMetricsResponse.builder()
            .totalProcessed(0)
            .totalSent(0)
            .totalFailed(0)
            .queueBacklog(0)
            .pendingCount(0)
            .failurePercentage(0.0)
            .successPercentage(0.0)
            .build();
    }

    double successPct = roundToTwoDecimals(((double) raw.getTotalSent() / raw.getTotalProcessed()) * 100);
    double failurePct = roundToTwoDecimals(((double) raw.getTotalFailed() / raw.getTotalProcessed()) * 100);

    return NotificationMetricsResponse.builder()
        .totalProcessed(raw.getTotalProcessed())
        .totalSent(raw.getTotalSent())
        .totalFailed(raw.getTotalFailed())
        .queueBacklog(raw.getQueueBacklog())
        .pendingCount(raw.getPendingCount())
        .successPercentage(successPct)
        .failurePercentage(failurePct)
        .build();
}
    private double roundToTwoDecimals(double value) {
        return BigDecimal.valueOf(value)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
    }

    /**
     * Inner mapping class for intermediate MongoDB aggregation raw results.
     */
    private static class MetricsQueryResult {
        private long totalProcessed;
        private long totalSent;
        private long totalFailed;
        private long queueBacklog;
        private long pendingCount;

        public long getTotalProcessed() { return totalProcessed; }

        public long getTotalSent() { return totalSent; }

        public long getTotalFailed() { return totalFailed; }

        public long getQueueBacklog() { return queueBacklog; }
        public long getPendingCount() { return pendingCount; }
    }
}