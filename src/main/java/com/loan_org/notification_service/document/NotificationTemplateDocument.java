package com.loan_org.notification_service.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notificationTemplates")
public class NotificationTemplateDocument {

    @Id
    private String id;

    private String templateCode;
    private String subjectLine;
    private String htmlContent;

    @LastModifiedDate
    private Instant updatedAt;
}
