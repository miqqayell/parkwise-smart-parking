package be.kdg.parkwise.repository;

import be.kdg.parkwise.domain.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingSpotRepo extends JpaRepository<ParkingSpot, Long> {
}
