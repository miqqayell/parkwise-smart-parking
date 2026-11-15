package be.kdg.parkwise.service;

import be.kdg.parkwise.domain.Car;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface CarService {

    Car registerCar(Long userId, String brand, String model, String licensePlate,
                    String color, MultipartFile document);

    List<Car> getUserCars(Long userId);

    Optional<Car> findById(Long carId);

    void deleteCar(Long carId, Long userId);

    boolean licensePlateExists(String licensePlate);
}