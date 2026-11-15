package be.kdg.parkwise.repository;

import be.kdg.parkwise.domain.SensorAlert;
import be.kdg.parkwise.domain.SensorDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link SensorAlert} entities.
 *
 * <p>Provides query methods for managing sensor inactivity alerts, including
 * deduplication checks and retrieval of active/resolved alerts.</p>
 */
public interface SensorAlertRepo extends JpaRepository<SensorAlert, Long> {

    /**
     * Finds the most recent unresolved alert for a specific sensor.
     *
     * @param sensor the sensor to check
     * @return the most recent unresolved alert, or empty if none exists
     */
    Optional<SensorAlert> findFirstBySensorAndResolvedFalseOrderByAlertTimeDesc(SensorDevice sensor);

    /**
     * Checks if an unresolved alert exists for a specific sensor.
     * Used for deduplication to prevent creating multiple alerts for the same issue.
     *
     * @param sensor the sensor to check
     * @return true if an unresolved alert exists, false otherwise
     */
    boolean existsBySensorAndResolvedFalse(SensorDevice sensor);

    /**
     * Retrieves all unresolved alerts, ordered by most recent first.
     *
     * @return list of unresolved alerts
     */
    List<SensorAlert> findByResolvedFalseOrderByAlertTimeDesc();

    /**
     * Retrieves alerts within a specific time range.
     *
     * @param start start time (inclusive)
     * @param end end time (inclusive)
     * @return list of alerts within the time range
     */
    List<SensorAlert> findByAlertTimeBetweenOrderByAlertTimeDesc(Instant start, Instant end);

    /**
     * Counts the number of unresolved alerts.
     *
     * @return count of unresolved alerts
     */
    long countByResolvedFalse();

    /**
     * Finds unresolved alerts that are older than a specific cutoff time.
     * Useful for identifying alerts that have been active for too long.
     *
     * @param cutoffTime alerts older than this time will be returned
     * @return list of old unresolved alerts
     */
    @Query("SELECT a FROM SensorAlert a WHERE a.resolved = false AND a.alertTime < :cutoffTime ORDER BY a.alertTime ASC")
    List<SensorAlert> findUnresolvedAlertsOlderThan(@Param("cutoffTime") Instant cutoffTime);
}
