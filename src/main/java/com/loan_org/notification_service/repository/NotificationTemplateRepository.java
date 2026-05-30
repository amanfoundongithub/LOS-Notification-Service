package com.loan_org.notification_service.repository;

import com.loan_org.notification_service.document.NotificationTemplateDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface NotificationTemplateRepository extends MongoRepository<NotificationTemplateDocument, String> {
    Optional<NotificationTemplateDocument> findByTemplateCode(String templateCode);
}
