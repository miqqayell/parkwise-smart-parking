package be.kdg.parkwise.repository;

import be.kdg.parkwise.domain.User;
import be.kdg.parkwise.domain.UserNotification;
import be.kdg.parkwise.domain.Enum.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * Repository for {@link UserNotification} entities.
 *
 * <p>Provides query methods for retrieving and managing in-app notifications
 * for users, including unread counts, pagination, and filtering by status.</p>
 */
public interface UserNotificationRepo extends JpaRepository<UserNotification, Long> {

    /**
     * Counts the number of notifications for a user with a specific status.
     * Primarily used for badge counts (UNREAD notifications).
     *
     * @param user the user
     * @param status the notification status
     * @return count of notifications with the specified status
     */
    long countByUserAndStatus(User user, NotificationStatus status);

    /**
     * Retrieves paginated notifications for a user, ordered by most recent first.
     *
     * @param user the user
     * @param pageable pagination parameters
     * @return page of notifications
     */
    Page<UserNotification> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    /**
     * Retrieves all notifications for a user with a specific status.
     *
     * @param user the user
     * @param status the notification status
     * @return list of notifications with the specified status
     */
    List<UserNotification> findByUserAndStatusOrderByCreatedAtDesc(User user, NotificationStatus status);

    /**
     * Retrieves recent notifications for a user created after a specific time.
     *
     * @param user the user
     * @param since only notifications created after this time will be returned
     * @return list of recent notifications
     */
    @Query("SELECT n FROM UserNotification n WHERE n.user = :user AND n.createdAt > :since ORDER BY n.createdAt DESC")
    List<UserNotification> findRecentNotifications(@Param("user") User user, @Param("since") Instant since);

    /**
     * Retrieves top N unread notifications for a user, ordered by priority and time.
     * Used for displaying the most important notifications in the dropdown.
     *
     * @param user the user
     * @param status the notification status (typically UNREAD)
     * @param pageable pagination parameters (use PageRequest.of(0, limit) for top N)
     * @return page of notifications ordered by priority (desc) then created time (desc)
     */
    Page<UserNotification> findByUserAndStatusOrderByPriorityDescCreatedAtDesc(
            User user, NotificationStatus status, Pageable pageable);
}
