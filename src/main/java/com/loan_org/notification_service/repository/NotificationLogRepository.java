package com.loan_org.notification_service.repository;

import com.loan_org.notification_service.document.NotificationLogDocument;
import com.loan_org.notification_service.domain.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationLogRepository extends MongoRepository<NotificationLogDocument, String> {

    Page<NotificationLogDocument>      findByUserId(String userId, Pageable pageable);
    List<NotificationLogDocument>      findByStatusAndNextRetryAtBefore(NotificationStatus status, Instant time);
    Optional<NotificationLogDocument>  findByProviderReferenceId(String providerReferenceId);
    Optional<NotificationLogDocument>  findByTransactionId(String transactionId);

}
