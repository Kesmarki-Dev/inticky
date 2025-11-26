package com.kesmarki.inticky.notification.enums;

/**
 * Enumeration for notification delivery status
 */
public enum NotificationStatus {
    PENDING,        // Queued for delivery
    PROCESSING,     // Currently being processed
    SENT,          // Successfully sent
    DELIVERED,     // Confirmed delivery
    FAILED,        // Failed to send
    BOUNCED,       // Email bounced
    REJECTED,      // Rejected by provider
    CANCELLED,     // Cancelled before sending
    EXPIRED,       // Expired before delivery
    OPENED,        // Email/push opened by recipient
    CLICKED        // Link clicked in notification
}
