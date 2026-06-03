package com.loan_org.notification_service.domain.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

/**
 * Persistent MongoDB document representation for storing and dynamically managing
 * raw communication layouts and subject structures parsed by the Thymeleaf rendering engine.
 *
 * @author amanfoundongithub
 * @version 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notificationTemplates")
public class NotificationTemplateDocument {

    /**
     * Unique mongo id
     */
    @Id
    private String id;

    /**
     * Code to write template
     */
    @Indexed(unique = true)
    private String templateCode;

    /**
     * Contents
     */
    private String subjectLine;
    private String htmlContent;

    /**
     * Version tracking meta-data
     */
    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    @Version
    @Builder.Default
    private Long version = 0L;

}
