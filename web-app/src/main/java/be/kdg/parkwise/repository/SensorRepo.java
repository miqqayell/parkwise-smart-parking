package be.kdg.parkwise.repository;

import be.kdg.parkwise.domain.ParkingSpot;
import be.kdg.parkwise.domain.SensorDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorRepo extends JpaRepository<SensorDevice, Long> {
    Optional<SensorDevice> findBySpot(ParkingSpot spot);
}
