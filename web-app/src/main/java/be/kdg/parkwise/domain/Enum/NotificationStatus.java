package be.kdg.parkwise.domain.Enum;

/**
 * Enum representing the status of a user notification.
 *
 * <p>Status lifecycle:</p>
 * <ul>
 *   <li>{@link #UNREAD} - Notification created but not yet viewed</li>
 *   <li>{@link #READ} - User has viewed the notification</li>
 *   <li>{@link #DISMISSED} - User has dismissed the notification</li>
 * </ul>
 */
public enum NotificationStatus {
    /**
     * Notification has not been viewed by the user.
     */
    UNREAD,

    /**
     * Notification has been viewed by the user.
     */
    READ,

    /**
     * Notification has been dismissed by the user.
     */
    DISMISSED
}
