package be.kdg.parkwise.dto;

import be.kdg.parkwise.domain.UserNotification;

import java.time.Duration;
import java.time.Instant;

/**
 * Data Transfer Object for user notifications.
 *
 * <p>Lightweight representation of a notification for API responses,
 * including a human-readable "timeAgo" field for display purposes.</p>
 *
 * @param id notification ID
 * @param type notification type (e.g., "SENSOR_ALERT")
 * @param title notification title
 * @param message notification message
 * @param status notification status (UNREAD, READ, DISMISSED)
 * @param priority notification priority (LOW, NORMAL, HIGH, CRITICAL)
 * @param relatedSensorId ID of related sensor (if applicable)
 * @param relatedAlertId ID of related alert (if applicable)
 * @param createdAt timestamp when notification was created
 * @param readAt timestamp when notification was read (null if unread)
 * @param timeAgo human-readable time since creation (e.g., "5 minutes ago")
 */
public record NotificationDto(
        Long id,
        String type,
        String title,
        String message,
        String status,
        String priority,
        Long relatedSensorId,
        Long relatedAlertId,
        Instant createdAt,
        Instant readAt,
        String timeAgo
) {
    /**
     * Converts a {@link UserNotification} entity to a DTO.
     *
     * @param notification the entity to convert
     * @return DTO representation
     */
    public static NotificationDto fromEntity(UserNotification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getNotificationType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getStatus().toString(),
                notification.getPriority().toString(),
                notification.getRelatedSensor() != null ? notification.getRelatedSensor().getId() : null,
                notification.getRelatedAlert() != null ? notification.getRelatedAlert().getId() : null,
                notification.getCreatedAt(),
                notification.getReadAt(),
                calculateTimeAgo(notification.getCreatedAt())
        );
    }

    /**
     * Calculates a human-readable "time ago" string.
     *
     * @param createdAt timestamp of creation
     * @return human-readable string (e.g., "5 minutes ago", "2 hours ago")
     */
    private static String calculateTimeAgo(Instant createdAt) {
        long minutes = Duration.between(createdAt, Instant.now()).toMinutes();

        if (minutes < 1) {
            return "Just now";
        } else if (minutes == 1) {
            return "1 minute ago";
        } else if (minutes < 60) {
            return minutes + " minutes ago";
        }

        long hours = minutes / 60;
        if (hours == 1) {
            return "1 hour ago";
        } else if (hours < 24) {
            return hours + " hours ago";
        }

        long days = hours / 24;
        if (days == 1) {
            return "1 day ago";
        } else {
            return days + " days ago";
        }
    }
}
