package com.loan_org.notification_service.shared.model;

/**
 * Defines the state machine lifecycle boundaries for outbound communications.
 * <p>These statuses track message progression from initial ingestion to downstream
 * delivery, facilitating transactional state tracking, automated retry eligibility
 * evaluations, and terminal auditing records.</p>
 *
 * <h3>State Transitions Flow:</h3>
 * <pre>
 * PENDING ──► PROCESSING ──► DELIVERED
 * │
 * ├──► RETRYING ──► ABANDONED (Max Retries Exhausted)
 * │
 * ├──► FAILED (Hard Error)
 * └──► BLOCKED_BY_PREFERENCE
 * </pre>
 *
 * @author amanfoundongithub
 * @version 1.0.0
 */
public enum NotificationStatus {
    /** The notification has been created and is waiting in a RabbitMQ queue. */
    PENDING,

    /** A consumer has picked up the message and is actively calling the third-party provider. */
    PROCESSING,

    /** The third-party provider (Twilio, AWS SES) successfully accepted the message. */
    DELIVERED,

    /** A hard failure occurred (e.g., invalid phone number format) where retrying is useless. */
    FAILED,

    /** A temporary failure occurred; the document is waiting for its scheduled 'nextRetryAt' timestamp. */
    RETRYING,

    /** The message failed repeatedly and exhausted all retry attempts. It is now dead-lettered. */
    ABANDONED,

    /** The notification was dropped because the user opted out of this specific template or channel. */
    BLOCKED_BY_PREFERENCE
}