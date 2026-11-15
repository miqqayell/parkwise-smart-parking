package be.kdg.parkwise.repository;

import be.kdg.parkwise.domain.SensorDevice;
import be.kdg.parkwise.domain.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorReadingRepo extends JpaRepository<SensorReading, Long> {
    Optional<SensorReading> findFirstBySensorOrderByReadingTimeDesc(SensorDevice sensor);
}
