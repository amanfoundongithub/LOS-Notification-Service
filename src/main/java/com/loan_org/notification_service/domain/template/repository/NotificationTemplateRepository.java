package com.loan_org.notification_service.domain.template.repository;

import com.loan_org.notification_service.domain.template.entity.NotificationTemplateDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

/**
 * Spring Data MongoDB repository infrastructure supplying direct data access,
 * mutation routines, and unique layout lookups for {@link NotificationTemplateDocument} assets.
 *
 * @author amanfoundongithub
 * @version 1.0.0
 */
public interface NotificationTemplateRepository extends MongoRepository<NotificationTemplateDocument, String> {

    /**
     * Resolves a unique communication template layout out of the collection using its
     * business signature token string.
     * <p>Utilized heavily by processing engines at runtime to fetch uncompiled markup designs
     * before variable binding execution takes place.</p>
     *
     * @param templateCode The unique look-up signature token referencing the layout (e.g., USER_ACTIVATION)
     * @return An Optional wrapper containing the matched layout configuration document if found
     */
    Optional<NotificationTemplateDocument> findByTemplateCode(String templateCode);
}