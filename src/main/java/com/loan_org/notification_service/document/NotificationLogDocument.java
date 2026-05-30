package com.loan_org.notification_service.document;

import com.loan_org.notification_service.domain.NotificationChannel;
import com.loan_org.notification_service.domain.NotificationPriority;
import com.loan_org.notification_service.domain.NotificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
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
    private String userId;        // userId lookup

    @Indexed
    private String transactionId; // id is required to keep logs of the transaction generator

    // Notification actual values for the considered value
    private String recipient;
    private NotificationChannel channel;
    private String title;
    private String templateCode;
    private Map<String, Object> templateVariables;

    // Metadata related to delivery of email
    private NotificationPriority priority;
    private Instant sentAt;
    private NotificationStatus status;

    @CreatedDate
    @Indexed(expireAfter = "30d")
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    // Retry metadata
    @Builder.Default
    private int retryCount = 0;

    @Builder.Default
    private int maxRetries = 3;

    private Instant nextRetryAt;

    // Error debugging for handy reference in DLQ
    private String providerReferenceId;
    private String errorCode;
    private String errorMessage;

    @Version
    private Long version;

}
