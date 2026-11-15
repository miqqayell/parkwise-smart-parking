package be.kdg.parkwise.controller;

import be.kdg.parkwise.domain.SensorDevice;
import be.kdg.parkwise.domain.SensorReading;
import be.kdg.parkwise.repository.SensorReadingRepo;
import be.kdg.parkwise.repository.SensorRepo;
import be.kdg.parkwise.service.LoggingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Controller
public class MonitoringController {

    private final SensorRepo sensorRepo;
    private final SensorReadingRepo readingRepo;
    private final LoggingService loggingService;

    public MonitoringController(SensorRepo sensorRepo, SensorReadingRepo readingRepo, LoggingService loggingService) {
        this.sensorRepo = sensorRepo;
        this.readingRepo = readingRepo;
        this.loggingService = loggingService;
    }

    @GetMapping("/admin/monitoring")
    public String monitoringPage(HttpSession session, Model model) {
        // Check authentication (existing pattern from UserController)
        AuthController.UserSessionView currentUser =
                (AuthController.UserSessionView) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/home";
        }

        // Check if user has system_admin role
        String userRole = (String) session.getAttribute("userRole");
        if (!"system_admin".equals(userRole)) {
            // Redirect to home if not admin
            return "redirect:/home";
        }

        model.addAttribute("user", currentUser);
        return "admin/monitoring";
    }

    @GetMapping("/api/sensors/status")
    @ResponseBody
    public List<Map<String, Object>> getAllSensorsStatus() {
        try {
            List<Map<String, Object>> result = new ArrayList<>();
            List<SensorDevice> sensors = sensorRepo.findAll();

            for (SensorDevice sensor : sensors) {
                try {
                    Map<String, Object> sensorData = new HashMap<>();

                    // Basic sensor info
                    sensorData.put("sensorId", sensor.getId());
                    sensorData.put("sensorType", sensor.getSensorType());
                    sensorData.put("isActive", sensor.isActive());

                    // Parking spot info
                    if (sensor.getSpot() != null) {
                        sensorData.put("spotId", sensor.getSpot().getId());
                        sensorData.put("spotNumber", sensor.getSpot().getSpotNumber());
                    } else {
                        sensorData.put("spotId", null);
                        sensorData.put("spotNumber", null);
                    }

                    // Get latest reading
                    Optional<SensorReading> latestReading =
                            readingRepo.findFirstBySensorOrderByReadingTimeDesc(sensor);

                    if (latestReading.isPresent()) {
                        SensorReading reading = latestReading.get();
                        Instant readingTime = reading.getReadingTime();

                        sensorData.put("lastPing", readingTime.toString());
                        sensorData.put("occupied", reading.getOccupied());

                        // Determine if sensor is online (reading within last 60 seconds)
                        Duration timeSinceReading = Duration.between(readingTime, Instant.now());
                        boolean isOnline = timeSinceReading.getSeconds() < 60;
                        sensorData.put("status", isOnline ? "ONLINE" : "OFFLINE");

                        // Log if sensor is offline
                        if (!isOnline) {
                            loggingService.logWarn("SENSOR_ERROR",
                                    "Sensor " + sensor.getId() + " is offline - last reading was " +
                                    timeSinceReading.getSeconds() + " seconds ago",
                                    sensor.getId(), sensor.getSpot() != null ? sensor.getSpot().getId() : null);
                        }
                    } else {
                        // No readings available
                        sensorData.put("lastPing", null);
                        sensorData.put("occupied", null);
                        sensorData.put("status", "OFFLINE");

                        loggingService.logWarn("SENSOR_ERROR",
                                "No readings available for sensor " + sensor.getId(),
                                sensor.getId(), sensor.getSpot() != null ? sensor.getSpot().getId() : null);
                    }

                    result.add(sensorData);

                } catch (Exception e) {
                    loggingService.logError("SYSTEM_ERROR",
                            "Error processing sensor " + sensor.getId(), e,
                            sensor.getId(), null);
                }
            }

            return result;

        } catch (Exception e) {
            loggingService.logError("SYSTEM_ERROR",
                    "Error fetching all sensors status", e, null, null);
            return new ArrayList<>();
        }
    }
}
