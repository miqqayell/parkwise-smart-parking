package be.kdg.parkwise.repository;

import be.kdg.parkwise.domain.ParkingArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingAreaRepo extends JpaRepository<ParkingArea, Long> {
}
