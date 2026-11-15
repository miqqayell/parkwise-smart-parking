package be.kdg.parkwise.repository;

import be.kdg.parkwise.domain.Car;
import be.kdg.parkwise.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    List<Car> findByUserAndActiveTrue(User user);

    List<Car> findByUserId(Long userId);

    Optional<Car> findByLicensePlate(String licensePlate);

    boolean existsByLicensePlate(String licensePlate);
}