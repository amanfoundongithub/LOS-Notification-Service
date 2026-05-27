package com.loan_org.notification_service.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notificationLogs")
@CompoundIndex(name = "idx_user_created_at", def = "{'userId': 1, 'createdAt': -1}")
@CompoundIndex(name = "idx_status_next_retry", def = "{'status': 1, 'nextRetryAt': 1}")
public class NotificationLogDocument {

    @Id
    private String id;

    @Indexed
    private String userId;
    private String transactionId;
    private String recipient;
    private String channel;      // EMAIL, SMS, PUSH
    private String templateCode;

    @Indexed
    private String status;       // PENDING, DELIVERED, FAILED, BLOCKED_BY_PREFERENCE
    private String priority;

    @Builder.Default
    private Instant createdAt = Instant.now();
    private Instant sentAt;

    @Builder.Default
    private int retryCount = 0;

    @Builder.Default
    private int maxRetries = 3;

    private Instant nextRetryAt;

    private String providerReferenceId;
    private String errorCode;
    private String errorMessage;

    private String title;
    private String content;
    private Map<String, Object> templateVariables;

    @Version
    private Long version;
}
