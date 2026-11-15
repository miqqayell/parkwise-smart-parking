package be.kdg.parkwise.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "system_logs")
public class SystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long id;

    @Column(name = "log_level", nullable = false)
    private String logLevel; // ERROR, WARN, INFO, DEBUG

    @Column(name = "log_category", nullable = false)
    private String logCategory; // SENSOR_ERROR, NETWORK_ERROR, DATA_ERROR, SYSTEM_ERROR, INFO

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // Stack trace, JSON data, etc.

    @Column(name = "source_class")
    private String sourceClass;

    @Column(name = "source_method")
    private String sourceMethod;

    @Column(name = "sensor_id")
    private Long sensorId;

    @Column(name = "spot_id")
    private Long spotId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "user_email")
    private String userEmail;

    public SystemLog() {
        this.createdAt = Instant.now();
    }

    public SystemLog(String logLevel, String logCategory, String message) {
        this.logLevel = logLevel;
        this.logCategory = logCategory;
        this.message = message;
        this.createdAt = Instant.now();
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getLogCategory() {
        return logCategory;
    }

    public void setLogCategory(String logCategory) {
        this.logCategory = logCategory;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getSourceClass() {
        return sourceClass;
    }

    public void setSourceClass(String sourceClass) {
        this.sourceClass = sourceClass;
    }

    public String getSourceMethod() {
        return sourceMethod;
    }

    public void setSourceMethod(String sourceMethod) {
        this.sourceMethod = sourceMethod;
    }

    public Long getSensorId() {
        return sensorId;
    }

    public void setSensorId(Long sensorId) {
        this.sensorId = sensorId;
    }

    public Long getSpotId() {
        return spotId;
    }

    public void setSpotId(Long spotId) {
        this.spotId = spotId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}
