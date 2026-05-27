package com.loan_org.notification_service.repository;

import com.loan_org.notification_service.document.NotificationLogDocument;
import com.loan_org.notification_service.domain.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface NotificationLogRepository extends MongoRepository<NotificationLogDocument, String> {

    Page<NotificationLogDocument> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);
    List<NotificationLogDocument> findByStatusAndNextRetryAtBefore(NotificationStatus status, Instant time);
    NotificationLogDocument       findByProviderReferenceId(String providerReferenceId);
    List<NotificationLogDocument> findByTransactionId(String transactionId);

}
