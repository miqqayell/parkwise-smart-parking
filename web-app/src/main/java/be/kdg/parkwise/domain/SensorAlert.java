package be.kdg.parkwise.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Entity representing a sensor inactivity alert.
 *
 * <p>Alerts are created when a sensor hasn't sent data for longer than
 * the configured threshold (default: 10 minutes). Each alert tracks when
 * it was created, how long the sensor was inactive, and whether/when it
 * was resolved by an administrator.</p>
 */
@Entity
@Table(name = "sensor_alerts")
public class SensorAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private SensorDevice sensor;

    @Column(name = "alert_time", nullable = false)
    private Instant alertTime;

    @Column(name = "last_reading_time")
    private Instant lastReadingTime;

    @Column(name = "minutes_inactive", nullable = false)
    private Integer minutesInactive;

    @Column(name = "alert_message", nullable = false, columnDefinition = "TEXT")
    private String alertMessage;

    @Column(name = "is_resolved", nullable = false)
    private boolean resolved = false;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private User resolvedBy;

    @Column(name = "resolution_notes", columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Default constructor for JPA.
     */
    public SensorAlert() {
        this.alertTime = Instant.now();
        this.createdAt = Instant.now();
    }

    /**
     * Constructor for creating new sensor alerts.
     *
     * @param sensor the sensor that triggered the alert
     * @param lastReadingTime timestamp of the sensor's last reading (null if never sent data)
     * @param minutesInactive number of minutes the sensor has been inactive
     */
    public SensorAlert(SensorDevice sensor, Instant lastReadingTime, int minutesInactive) {
        this();
        this.sensor = sensor;
        this.lastReadingTime = lastReadingTime;
        this.minutesInactive = minutesInactive;
        this.alertMessage = buildAlertMessage(sensor, minutesInactive, lastReadingTime);
    }

    /**
     * Builds a human-readable alert message.
     */
    private String buildAlertMessage(SensorDevice sensor, int minutes, Instant lastReading) {
        String spotInfo = sensor.getSpot() != null
                ? " (Spot #" + sensor.getSpot().getSpotNumber() + ")"
                : "";

        if (lastReading == null) {
            return String.format("Sensor %d%s has never sent data (inactive for %d+ minutes)",
                    sensor.getId(), spotInfo, minutes);
        } else {
            return String.format("Sensor %d%s has been inactive for %d minutes. Last reading: %s",
                    sensor.getId(), spotInfo, minutes, lastReading.toString());
        }
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SensorDevice getSensor() {
        return sensor;
    }

    public void setSensor(SensorDevice sensor) {
        this.sensor = sensor;
    }

    public Instant getAlertTime() {
        return alertTime;
    }

    public void setAlertTime(Instant alertTime) {
        this.alertTime = alertTime;
    }

    public Instant getLastReadingTime() {
        return lastReadingTime;
    }

    public void setLastReadingTime(Instant lastReadingTime) {
        this.lastReadingTime = lastReadingTime;
    }

    public Integer getMinutesInactive() {
        return minutesInactive;
    }

    public void setMinutesInactive(Integer minutesInactive) {
        this.minutesInactive = minutesInactive;
    }

    public String getAlertMessage() {
        return alertMessage;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public Instant getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(Instant resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public User getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(User resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
