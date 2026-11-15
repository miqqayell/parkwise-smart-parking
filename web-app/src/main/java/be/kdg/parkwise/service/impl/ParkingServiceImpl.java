package be.kdg.parkwise.service.impl;

import be.kdg.parkwise.domain.ParkingSpot;
import be.kdg.parkwise.domain.valueobject.ParkingSpotStatus;
import be.kdg.parkwise.repository.ParkingSpotRepo;
import be.kdg.parkwise.repository.SensorRepo;
import be.kdg.parkwise.repository.SensorReadingRepo;
import be.kdg.parkwise.service.LoggingService;
import be.kdg.parkwise.service.ParkingService;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;

/**
 * Service providing operations over parking spots and resolving their latest status
 * based on sensor readings. Uses transactional boundaries for consistency.
 */
@Service
@Transactional
public class ParkingServiceImpl implements ParkingService {

    private final ParkingSpotRepo parkingSpotRepo;
    private final SensorRepo sensorRepo;
    private final SensorReadingRepo readingRepo;
    private final EntityManager entityManager;
    private final LoggingService loggingService;

    public ParkingServiceImpl(ParkingSpotRepo parkingSpotRepo,
                              SensorRepo sensorRepo,
                              SensorReadingRepo readingRepo,
                              EntityManager entityManager,
                              LoggingService loggingService) {
        this.parkingSpotRepo = parkingSpotRepo;
        this.sensorRepo = sensorRepo;
        this.readingRepo = readingRepo;
        this.entityManager = entityManager;
        this.loggingService = loggingService;
    }

    // --------------------------------------------------------
    // BASIC CRUD
    // --------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Collection<ParkingSpot> findAll() {
        return parkingSpotRepo.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ParkingSpot> findById(Long id) {
        return parkingSpotRepo.findById(id);
    }

    @Override
    public ParkingSpot upsert(ParkingSpot spot) {
        return parkingSpotRepo.save(spot);
    }

    @Override
    public void updateStatus(Long id, ParkingSpotStatus status) {
        parkingSpotRepo.findById(id).ifPresent(spot -> {
            spot.setStatus(status);
            spot.setLastUpdated(Instant.now());
            parkingSpotRepo.save(spot);
        });
    }

    // --------------------------------------------------------
    // LATEST SENSOR STATUS LOOKUP (MOST IMPORTANT PART)
    // --------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public ParkingSpotStatus getLatestStatus(Long spotId) {
        try {
            // IMPORTANT:
            // Clear the JPA persistence cache so relationships (spot -> sensor)
            // reflect the latest database values after you run:
            // UPDATE sensors SET spot_id = X WHERE sensor_id = 647
            entityManager.clear();

            return parkingSpotRepo.findById(spotId).map(spot -> {

                // 1️⃣ Find the sensor linked to this parking spot
                var sensorOpt = sensorRepo.findBySpot(spot);
                if (sensorOpt.isEmpty()) {
                    loggingService.logWarn("SENSOR_ERROR",
                            "No sensor found for parking spot " + spotId, null, spotId);
                    return ParkingSpotStatus.UNKNOWN;
                }

                var sensor = sensorOpt.get();

                // 2️⃣ Get the latest reading for that sensor
                var latestReading = readingRepo.findFirstBySensorOrderByReadingTimeDesc(sensor);
                if (latestReading.isEmpty()) {
                    loggingService.logWarn("SENSOR_ERROR",
                            "No readings found for sensor " + sensor.getId() + " on spot " + spotId,
                            sensor.getId(), spotId);
                    return ParkingSpotStatus.UNKNOWN;
                }

                var reading = latestReading.get();

                // 3️⃣ Determine final status based on occupied flag
                Boolean occupied = reading.getOccupied();
                if (occupied == null) {
                    loggingService.logWarn("DATA_ERROR",
                            "Null occupancy value for sensor " + sensor.getId() + " on spot " + spotId,
                            sensor.getId(), spotId);
                    return ParkingSpotStatus.UNKNOWN;
                }

                return occupied ? ParkingSpotStatus.OCCUPIED : ParkingSpotStatus.FREE;

            }).orElse(ParkingSpotStatus.UNKNOWN);

        } catch (Exception e) {
            loggingService.logError("SYSTEM_ERROR",
                    "Error getting latest status for spot " + spotId, e, null, spotId);
            return ParkingSpotStatus.UNKNOWN;
        }
    }
}
