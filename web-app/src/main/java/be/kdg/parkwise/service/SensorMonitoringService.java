package be.kdg.parkwise.service;

import be.kdg.parkwise.domain.SensorAlert;

import java.util.List;

/**
 * Service interface for monitoring sensor health and generating alerts.
 *
 * <p>This service includes a scheduled task that runs every 5 minutes to check
 * for inactive sensors. When a sensor hasn't sent data for longer than the
 * configured threshold (default: 10 minutes), an alert is created and system
 * administrators are notified.</p>
 */
public interface SensorMonitoringService {

    /**
     * Scheduled task: Checks all sensors for inactivity.
     * Runs automatically every 5 minutes.
     *
     * <p>For each active sensor:</p>
     * <ul>
     *   <li>Retrieves the latest reading timestamp</li>
     *   <li>Checks if the sensor is inactive (no reading > threshold minutes)</li>
     *   <li>Creates an alert if inactive and no unresolved alert exists</li>
     *   <li>Notifies all system administrators</li>
     * </ul>
     */
    void checkSensorInactivity();

    /**
     * Manually triggers a sensor inactivity check.
     * Used for testing or admin-initiated checks.
     *
     * @return list of newly created alerts
     */
    List<SensorAlert> performManualCheck();

    /**
     * Resolves a sensor alert.
     *
     * @param alertId the alert ID to resolve
     * @param userId the ID of the user resolving the alert
     * @param resolutionNotes optional notes about how the issue was resolved
     * @throws IllegalArgumentException if alert or user not found
     */
    void resolveAlert(Long alertId, Long userId, String resolutionNotes);

    /**
     * Gets all active (unresolved) alerts.
     *
     * @return list of active alerts, ordered by most recent first
     */
    List<SensorAlert> getActiveAlerts();
}
