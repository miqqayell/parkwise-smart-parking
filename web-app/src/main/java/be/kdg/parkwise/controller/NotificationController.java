package be.kdg.parkwise.controller;

import be.kdg.parkwise.domain.User;
import be.kdg.parkwise.domain.UserNotification;
import be.kdg.parkwise.dto.NotificationDto;
import be.kdg.parkwise.repository.UserRepository;
import be.kdg.parkwise.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for managing user notifications.
 *
 * <p>Provides endpoints for retrieving notification counts, viewing notifications,
 * and updating notification status (read/dismissed). All endpoints require
 * authenticated users.</p>
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    public NotificationController(NotificationService notificationService, UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    /**
     * Gets the unread notification count for the current user.
     * Used for the notification badge display.
     *
     * @param session HTTP session containing current user
     * @return JSON object with "count" field
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        long count = notificationService.getUnreadCount(currentUser);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * Gets recent unread notifications for the dropdown display.
     *
     * @param session HTTP session containing current user
     * @param limit maximum number of notifications to return (default: 5)
     * @return list of recent unread notifications
     */
    @GetMapping("/recent")
    public ResponseEntity<List<NotificationDto>> getRecentNotifications(
            HttpSession session,
            @RequestParam(defaultValue = "5") int limit) {

        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        List<UserNotification> notifications = notificationService.getRecentUnread(currentUser, limit);
        List<NotificationDto> dtos = notifications.stream()
                .map(NotificationDto::fromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    /**
     * Gets paginated notifications for the full notifications page.
     *
     * @param session HTTP session containing current user
     * @param page page number (0-indexed, default: 0)
     * @param size page size (default: 20)
     * @return page of notifications
     */
    @GetMapping
    public ResponseEntity<Page<NotificationDto>> getNotifications(
            HttpSession session,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        Page<UserNotification> notifications = notificationService.getUserNotifications(currentUser, page, size);
        Page<NotificationDto> dtos = notifications.map(NotificationDto::fromEntity);

        return ResponseEntity.ok(dtos);
    }

    /**
     * Marks a notification as read.
     *
     * @param id notification ID
     * @param session HTTP session containing current user
     * @return 200 OK on success, 403 Forbidden if not authorized, 404 Not Found if doesn't exist
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            notificationService.markAsRead(id, currentUser);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Dismisses a notification.
     *
     * @param id notification ID
     * @param session HTTP session containing current user
     * @return 200 OK on success, 403 Forbidden if not authorized, 404 Not Found if doesn't exist
     */
    @PostMapping("/{id}/dismiss")
    public ResponseEntity<Void> dismissNotification(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            notificationService.dismissNotification(id, currentUser);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Marks all unread notifications as read for the current user.
     *
     * @param session HTTP session containing current user
     * @return 200 OK on success, 401 Unauthorized if not logged in
     */
    @PostMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (currentUser == null) {
            return ResponseEntity.status(401).build();
        }

        notificationService.markAllAsRead(currentUser);
        return ResponseEntity.ok().build();
    }

    /**
     * Helper method to get the current user from the HTTP session.
     *
     * @param session HTTP session
     * @return current user, or null if not logged in
     */
    private User getCurrentUser(HttpSession session) {
        AuthController.UserSessionView sessionUser =
                (AuthController.UserSessionView) session.getAttribute("currentUser");

        if (sessionUser == null) {
            return null;
        }

        return userRepository.findById(sessionUser.getId()).orElse(null);
    }
}
