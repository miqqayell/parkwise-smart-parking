package be.kdg.parkwise.domain.Enum;

/**
 * Enum representing the priority level of a notification.
 *
 * <p>Priority levels determine the visual presentation and sort order
 * of notifications in the user interface.</p>
 */
public enum NotificationPriority {
    /**
     * Low priority - informational notifications.
     */
    LOW,

    /**
     * Normal priority - standard notifications.
     */
    NORMAL,

    /**
     * High priority - important notifications requiring attention.
     * Used for sensor alerts.
     */
    HIGH,

    /**
     * Critical priority - urgent notifications requiring immediate action.
     */
    CRITICAL
}
