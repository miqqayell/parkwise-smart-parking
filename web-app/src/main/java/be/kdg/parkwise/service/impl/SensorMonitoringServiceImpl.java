package be.kdg.parkwise.service.impl;

import be.kdg.parkwise.domain.SensorAlert;
import be.kdg.parkwise.domain.SensorDevice;
import be.kdg.parkwise.domain.SensorReading;
import be.kdg.parkwise.domain.User;
import be.kdg.parkwise.repository.SensorAlertRepo;
import be.kdg.parkwise.repository.SensorReadingRepo;
import be.kdg.parkwise.repository.SensorRepo;
import be.kdg.parkwise.repository.UserRepository;
import be.kdg.parkwise.service.LoggingService;
import be.kdg.parkwise.service.NotificationService;
import be.kdg.parkwise.service.SensorMonitoringService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of {@link SensorMonitoringService}.
 *
 * <p>Monitors all active sensors and creates alerts when they become inactive.
 * The main monitoring loop runs as a scheduled task every 5 minutes.</p>
 */
@Service
public class SensorMonitoringServiceImpl implements SensorMonitoringService {

    private final SensorRepo sensorRepo;
    private final SensorReadingRepo sensorReadingRepo;
    private final SensorAlertRepo sensorAlertRepo;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final LoggingService loggingService;

    @Value("${app.monitoring.inactivity-threshold-minutes:10}")
    private int inactivityThresholdMinutes;

    public SensorMonitoringServiceImpl(
            SensorRepo sensorRepo,
            SensorReadingRepo sensorReadingRepo,
            SensorAlertRepo sensorAlertRepo,
            UserRepository userRepository,
            NotificationService notificationService,
            LoggingService loggingService) {
        this.sensorRepo = sensorRepo;
        this.sensorReadingRepo = sensorReadingRepo;
        this.sensorAlertRepo = sensorAlertRepo;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.loggingService = loggingService;
    }

    @Override
    @Scheduled(fixedRate = 300000) // Run every 5 minutes (300,000 milliseconds)
    @Transactional
    public void checkSensorInactivity() {
        try {
            loggingService.logInfo("INFO",
                    "Starting scheduled sensor inactivity check (threshold: " + inactivityThresholdMinutes + " minutes)");

            List<SensorDevice> allSensors = sensorRepo.findAll();
            Instant now = Instant.now();
            Instant cutoffTime = now.minus(Duration.ofMinutes(inactivityThresholdMinutes));

            int alertsCreated = 0;

            for (SensorDevice sensor : allSensors) {
                // Skip inactive sensors (those marked as not active in the database)
                if (!sensor.isActive()) {
                    continue;
                }

                // Get the latest reading for this sensor
                Optional<SensorReading> latestReading =
                        sensorReadingRepo.findFirstBySensorOrderByReadingTimeDesc(sensor);

                boolean isInactive = false;
                Instant lastReadingTime = null;

                if (latestReading.isEmpty()) {
                    // Sensor has NEVER sent data
                    isInactive = true;
                } else {
                    lastReadingTime = latestReading.get().getReadingTime();
                    if (lastReadingTime.isBefore(cutoffTime)) {
                        // Sensor hasn't sent data in more than threshold minutes
                        isInactive = true;
                    }
                }

                if (isInactive) {
                    // Check if an unresolved alert already exists (deduplication)
                    boolean hasUnresolvedAlert = sensorAlertRepo.existsBySensorAndResolvedFalse(sensor);

                    if (!hasUnresolvedAlert) {
                        // Create new alert
                        int minutesInactive = calculateMinutesInactive(lastReadingTime, now);
                        SensorAlert alert = new SensorAlert(sensor, lastReadingTime, minutesInactive);
                        sensorAlertRepo.save(alert);

                        // Notify all system administrators
                        notifySystemAdmins(alert);

                        alertsCreated++;

                        loggingService.logWarn("SENSOR_ERROR",
                                "Sensor " + sensor.getId() + " inactive for " + minutesInactive + " minutes. Alert created.",
                                sensor.getId(),
                                sensor.getSpot() != null ? sensor.getSpot().getId() : null);
                    } else {
                        // Alert already exists - skip to prevent spam
                        loggingService.logInfo("INFO",
                                "Sensor " + sensor.getId() + " is inactive but alert already exists. Skipping duplicate alert.");
                    }
                }
            }

            loggingService.logInfo("INFO",
                    "Sensor inactivity check completed. Checked " + allSensors.size() +
                            " sensors, created " + alertsCreated + " new alerts.");

        } catch (Exception e) {
            loggingService.logError("SYSTEM_ERROR",
                    "Error during scheduled sensor monitoring check", e, null, null);
        }
    }

    /**
     * Calculates how many minutes a sensor has been inactive.
     *
     * @param lastReading timestamp of last reading (null if never sent data)
     * @param now current timestamp
     * @return minutes inactive (Integer.MAX_VALUE if never received data)
     */
    private int calculateMinutesInactive(Instant lastReading, Instant now) {
        if (lastReading == null) {
            return Integer.MAX_VALUE; // Sensor has never sent data
        }
        Duration duration = Duration.between(lastReading, now);
        return (int) duration.toMinutes();
    }

    /**
     * Notifies all system administrators about a new sensor alert.
     *
     * @param alert the alert to notify about
     */
    private void notifySystemAdmins(SensorAlert alert) {
        List<User> systemAdmins = userRepository.findByRole("system_admin");

        if (systemAdmins.isEmpty()) {
            loggingService.logWarn("SYSTEM_ERROR",
                    "No system administrators found to notify about sensor alert " + alert.getId());
            return;
        }

        for (User admin : systemAdmins) {
            try {
                notificationService.createSensorAlertNotification(admin, alert);
            } catch (Exception e) {
                loggingService.logError("SYSTEM_ERROR",
                        "Failed to create notification for admin " + admin.getId() +
                                " for alert " + alert.getId(), e, null, null);
            }
        }

        loggingService.logInfo("INFO",
                "Notified " + systemAdmins.size() + " system administrators about sensor alert " + alert.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorAlert> performManualCheck() {
        loggingService.logInfo("INFO", "Manual sensor inactivity check initiated");

        // For manual check, just return active alerts rather than creating new ones
        // Admins can use this to see current alert status
        return getActiveAlerts();
    }

    @Override
    @Transactional
    public void resolveAlert(Long alertId, Long userId, String resolutionNotes) {
        SensorAlert alert = sensorAlertRepo.findById(alertId)
                .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        alert.setResolved(true);
        alert.setResolvedAt(Instant.now());
        alert.setResolvedBy(user);
        alert.setResolutionNotes(resolutionNotes);

        sensorAlertRepo.save(alert);

        loggingService.logInfo("INFO",
                "Alert " + alertId + " resolved by user " + userId +
                        (resolutionNotes != null ? ". Notes: " + resolutionNotes : ""));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SensorAlert> getActiveAlerts() {
        return sensorAlertRepo.findByResolvedFalseOrderByAlertTimeDesc();
    }
}
