package com.loan_org.notification_service.domain.audit;

import com.loan_org.notification_service.domain.channels.NotificationChannel;
import com.loan_org.notification_service.shared.model.NotificationPriority;
import com.loan_org.notification_service.shared.model.NotificationStatus;

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

/**
 * Persistent MongoDB document representation for tracking, auditing, and managing
 * the operational lifecycle of communication dispatches within the Notification Service.
 * <p>This document acts as an audit trail for inbound notification payloads routed via
 * AMQP, recording recipient identities, compiled payload contextual data, and direct state machine
 * progressions. It also stores fault-tolerance retry metadata utilized by scheduled recovery
 * polling jobs during downstream delivery outages.</p>
 *
 * <h3>Database Optimization Strategy:</h3>
 * <ul>
 * <li><b>idx_user_created_at:</b> Compound index facilitating quick historical user timeline sorting
 * and rendering inside dashboard timeline feeds.</li>
 * <li><b>idx_status_next_retry:</b> Compound index backing efficient poll schedules that query
 * eligible failed records requiring systematic retry executions.</li>
 * <li><b>TTL Expiry:</b> Automatically purged 30 days past creation date via a dedicated background index thread.</li>
 * </ul>
 *
 * @author amanfoundongithub
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notificationLogs")
@CompoundIndex(name = "idx_user_created_at", def = "{'userId': 1, 'createdAt': -1}")
@CompoundIndex(name = "idx_status_next_retry", def = "{'status': 1, 'nextRetryAt': 1}")
public class NotificationLogDocument {

    /**
     * Defines the unique MongoID of the document.
     */
    @Id
    private String id;

    /**
     * Useful for tracing targeted user, if any.
     */
    @Indexed
    private String userId;

    /**
     * Useful for microservice traceability
     */
    @Indexed
    private String transactionId;

    // Notification-level content parameters
    @Indexed
    private String recipient;

    private NotificationChannel channel;
    private String title;
    private String templateCode;
    private Map<String, Object> templateVariables;

    // Notification-level metadata for sending email
    private NotificationPriority priority;
    private Instant sentAt;
    private NotificationStatus status;

    /**
     * TTL indexes for 30 days expiration window.
     */
    @Indexed(expireAfter = "30d")
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    // Retry metadata
    @Builder.Default
    private int retryCount = 0;

    @Builder.Default
    private int maxRetries = 3;

    private Instant nextRetryAt;

    /**
     * Debugging metadata from the transport provider.
     */
    private String providerReferenceId;
    private String errorCode;
    private String errorMessage;

    /**
     * Prevention against concurrent data writing by multiple servers.
     */
    @Version
    private Long version;

}
