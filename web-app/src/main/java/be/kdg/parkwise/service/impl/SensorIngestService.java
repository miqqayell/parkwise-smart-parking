package be.kdg.parkwise.service.impl;

import be.kdg.parkwise.domain.valueobject.ParkingSpotStatus;
import be.kdg.parkwise.service.LoggingService;
import be.kdg.parkwise.service.ParkingService;
import org.springframework.stereotype.Service;

@Service
public class SensorIngestService {
    private static final double DISTANCE_THRESHOLD_CM = 25.0;

    private final ParkingService parkingService;
    private final LoggingService loggingService;

    public SensorIngestService(ParkingService parkingService, LoggingService loggingService) {
        this.parkingService = parkingService;
        this.loggingService = loggingService;
    }

    public ParkingSpotStatus fuseAndUpdate(Long spotId, boolean magneticTriggered, double distanceCm) {
        try {
            boolean occupied = magneticTriggered || distanceCm < DISTANCE_THRESHOLD_CM;
            ParkingSpotStatus status = occupied ? ParkingSpotStatus.OCCUPIED : ParkingSpotStatus.FREE;

            // Log sensor fusion decision
            String fusionDetails = String.format("Spot %d: magnetic=%b, distance=%.2fcm, threshold=%.2fcm, result=%s",
                    spotId, magneticTriggered, distanceCm, DISTANCE_THRESHOLD_CM, status);
            loggingService.logInfo("SENSOR_DATA", fusionDetails, null, spotId);

            parkingService.updateStatus(spotId, status);
            return status;

        } catch (Exception e) {
            loggingService.logError("SENSOR_ERROR",
                    "Failed to fuse and update spot " + spotId, e, null, spotId);
            return ParkingSpotStatus.UNKNOWN;
        }
    }
}
