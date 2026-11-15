package be.kdg.parkwise.service.impl;

import be.kdg.parkwise.domain.Car;
import be.kdg.parkwise.domain.User;
import be.kdg.parkwise.repository.CarRepository;
import be.kdg.parkwise.repository.UserRepository;
import be.kdg.parkwise.service.CarService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class CarServiceImpl implements CarService {

    private final CarRepository carRepository;
    private final UserRepository userRepository;
    // Use /tmp directory which has write permissions on all systems
    private final String uploadDir = System.getProperty("user.home") + "/.parkwise/uploads/documents/";

    public CarServiceImpl(CarRepository carRepository, UserRepository userRepository) {
        this.carRepository = carRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Car registerCar(Long userId, String brand, String model, String licensePlate,
                           String color, MultipartFile document) {

        // Validate user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Validate license plate is unique
        if (carRepository.existsByLicensePlate(licensePlate)) {
            throw new IllegalArgumentException("A car with this license plate is already registered");
        }

        // Create new car
        Car car = new Car(user, brand, model, licensePlate, color);

        // Handle document upload if provided
        if (document != null && !document.isEmpty()) {
            String documentPath = saveDocument(document, userId, licensePlate);
            car.setDocumentPath(documentPath);
        }

        return carRepository.save(car);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Car> getUserCars(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return carRepository.findByUserAndActiveTrue(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Car> findById(Long carId) {
        return carRepository.findById(carId);
    }

    @Override
    public void deleteCar(Long carId, Long userId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new IllegalArgumentException("Car not found"));

        // Verify the car belongs to the user
        if (!car.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("You don't have permission to delete this car");
        }

        // Soft delete
        car.setActive(false);
        carRepository.save(car);

        // Optionally delete the document file
        if (car.getDocumentPath() != null) {
            try {
                Files.deleteIfExists(Paths.get(car.getDocumentPath()));
            } catch (IOException e) {
                // Log error but don't fail the operation
                System.err.println("Failed to delete document file: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean licensePlateExists(String licensePlate) {
        return carRepository.existsByLicensePlate(licensePlate);
    }

    private String saveDocument(MultipartFile file, Long userId, String licensePlate) {
        try {
            // Create upload directory if it doesn't exist
            Files.createDirectories(Paths.get(uploadDir));

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : "";
            String filename = "user_" + userId + "_" + licensePlate.replaceAll("[^a-zA-Z0-9]", "_")
                    + "_" + UUID.randomUUID().toString() + extension;

            // Save file
            Path targetLocation = Paths.get(uploadDir + filename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return targetLocation.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store document file", e);
        }
    }
}