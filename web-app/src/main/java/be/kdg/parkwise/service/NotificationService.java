package be.kdg.parkwise.service;

import be.kdg.parkwise.domain.SensorAlert;
import be.kdg.parkwise.domain.User;
import be.kdg.parkwise.domain.UserNotification;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Service interface for managing user notifications.
 *
 * <p>Provides methods for creating, retrieving, and updating in-app notifications.
 * All operations include proper security checks to ensure users can only access
 * their own notifications.</p>
 */
public interface NotificationService {

    /**
     * Creates a sensor alert notification for a user.
     *
     * @param user the user to notify
     * @param alert the sensor alert that triggered the notification
     * @return the created notification
     */
    UserNotification createSensorAlertNotification(User user, SensorAlert alert);

    /**
     * Gets the count of unread notifications for a user.
     * Used for notification badge display.
     *
     * @param user the user
     * @return count of unread notifications
     */
    long getUnreadCount(User user);

    /**
     * Gets paginated notifications for a user.
     *
     * @param user the user
     * @param page page number (0-indexed)
     * @param size page size
     * @return page of notifications
     */
    Page<UserNotification> getUserNotifications(User user, int page, int size);

    /**
     * Marks a notification as read.
     *
     * @param notificationId the notification ID
     * @param user the user (for security verification)
     * @throws IllegalArgumentException if notification not found
     * @throws SecurityException if notification doesn't belong to user
     */
    void markAsRead(Long notificationId, User user);

    /**
     * Dismisses a notification.
     *
     * @param notificationId the notification ID
     * @param user the user (for security verification)
     * @throws IllegalArgumentException if notification not found
     * @throws SecurityException if notification doesn't belong to user
     */
    void dismissNotification(Long notificationId, User user);

    /**
     * Marks all unread notifications as read for a user.
     *
     * @param user the user
     */
    void markAllAsRead(User user);

    /**
     * Gets recent unread notifications for dropdown display.
     *
     * @param user the user
     * @param limit maximum number of notifications to return
     * @return list of recent unread notifications, ordered by priority and time
     */
    List<UserNotification> getRecentUnread(User user, int limit);
}
