package be.kdg.parkwise.service;

import be.kdg.parkwise.domain.ParkingSpot;
import be.kdg.parkwise.domain.valueobject.ParkingSpotStatus;

import java.util.Collection;
import java.util.Optional;

public interface ParkingService {
    Collection<ParkingSpot> findAll();
    Optional<ParkingSpot> findById(Long id);
    ParkingSpot upsert(ParkingSpot spot);
    void updateStatus(Long id, ParkingSpotStatus status);

    // NEW: Ask the DB for the latest status based on sensor readings
    ParkingSpotStatus getLatestStatus(Long spotId);
}
