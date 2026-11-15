package be.kdg.parkwise.domain;

import be.kdg.parkwise.domain.Enum.NotificationPriority;
import be.kdg.parkwise.domain.Enum.NotificationStatus;
import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity representing an in-app notification for a user.
 *
 * <p>Notifications are created when system events occur that require user attention,
 * such as sensor alerts. Each notification tracks its read/dismissed status and can
 * be linked to related entities like sensors or alerts.</p>
 */
@Entity
@Table(name = "user_notifications")
public class UserNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "notification_type", nullable = false, length = 50)
    private String notificationType;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_sensor_id")
    private SensorDevice relatedSensor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_alert_id")
    private SensorAlert relatedAlert;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationStatus status = NotificationStatus.UNREAD;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private NotificationPriority priority = NotificationPriority.NORMAL;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "read_at")
    private Instant readAt;

    @Column(name = "dismissed_at")
    private Instant dismissedAt;

    /**
     * Default constructor for JPA.
     */
    public UserNotification() {
        this.createdAt = Instant.now();
    }

    /**
     * Factory method for creating sensor alert notifications.
     *
     * @param user the user to notify
     * @param alert the sensor alert that triggered the notification
     * @return a new UserNotification configured for a sensor alert
     */
    public static UserNotification createSensorAlert(User user, SensorAlert alert) {
        UserNotification notification = new UserNotification();
        notification.setUser(user);
        notification.setNotificationType("SENSOR_ALERT");
        notification.setTitle("Sensor Inactive");
        notification.setMessage(alert.getAlertMessage());
        notification.setRelatedSensor(alert.getSensor());
        notification.setRelatedAlert(alert);
        notification.setPriority(NotificationPriority.HIGH);
        return notification;
    }

    /**
     * Marks the notification as read.
     */
    public void markAsRead() {
        this.status = NotificationStatus.READ;
        this.readAt = Instant.now();
    }

    /**
     * Dismisses the notification.
     */
    public void dismiss() {
        this.status = NotificationStatus.DISMISSED;
        this.dismissedAt = Instant.now();
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SensorDevice getRelatedSensor() {
        return relatedSensor;
    }

    public void setRelatedSensor(SensorDevice relatedSensor) {
        this.relatedSensor = relatedSensor;
    }

    public SensorAlert getRelatedAlert() {
        return relatedAlert;
    }

    public void setRelatedAlert(SensorAlert relatedAlert) {
        this.relatedAlert = relatedAlert;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public void setStatus(NotificationStatus status) {
        this.status = status;
    }

    public NotificationPriority getPriority() {
        return priority;
    }

    public void setPriority(NotificationPriority priority) {
        this.priority = priority;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getReadAt() {
        return readAt;
    }

    public void setReadAt(Instant readAt) {
        this.readAt = readAt;
    }

    public Instant getDismissedAt() {
        return dismissedAt;
    }

    public void setDismissedAt(Instant dismissedAt) {
        this.dismissedAt = dismissedAt;
    }
}
