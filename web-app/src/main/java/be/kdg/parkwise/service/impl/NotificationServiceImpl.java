package be.kdg.parkwise.service.impl;

import be.kdg.parkwise.domain.Enum.NotificationStatus;
import be.kdg.parkwise.domain.SensorAlert;
import be.kdg.parkwise.domain.User;
import be.kdg.parkwise.domain.UserNotification;
import be.kdg.parkwise.repository.UserNotificationRepo;
import be.kdg.parkwise.service.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of {@link NotificationService}.
 *
 * <p>Manages in-app notifications for users, including creation, retrieval,
 * and status updates (read/dismissed). All operations include security checks
 * to ensure users can only access their own notifications.</p>
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private final UserNotificationRepo notificationRepo;

    public NotificationServiceImpl(UserNotificationRepo notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    @Override
    @Transactional
    public UserNotification createSensorAlertNotification(User user, SensorAlert alert) {
        UserNotification notification = UserNotification.createSensorAlert(user, alert);
        return notificationRepo.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(User user) {
        return notificationRepo.countByUserAndStatus(user, NotificationStatus.UNREAD);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserNotification> getUserNotifications(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepo.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, User user) {
        UserNotification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        // Security check: ensure notification belongs to user
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized access to notification " + notificationId);
        }

        notification.markAsRead();
        notificationRepo.save(notification);
    }

    @Override
    @Transactional
    public void dismissNotification(Long notificationId, User user) {
        UserNotification notification = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found: " + notificationId));

        // Security check: ensure notification belongs to user
        if (!notification.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized access to notification " + notificationId);
        }

        notification.dismiss();
        notificationRepo.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(User user) {
        List<UserNotification> unreadNotifications = notificationRepo
                .findByUserAndStatusOrderByCreatedAtDesc(user, NotificationStatus.UNREAD);

        for (UserNotification notification : unreadNotifications) {
            notification.markAsRead();
        }

        notificationRepo.saveAll(unreadNotifications);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserNotification> getRecentUnread(User user, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<UserNotification> page = notificationRepo
                .findByUserAndStatusOrderByPriorityDescCreatedAtDesc(
                        user, NotificationStatus.UNREAD, pageable);
        return page.getContent();
    }
}
