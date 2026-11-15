package be.kdg.parkwise.controller;

import be.kdg.parkwise.domain.Car;
import be.kdg.parkwise.service.CarService;
import be.kdg.parkwise.service.SubscriptionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class UserController {

    private final SubscriptionService subscriptionService;
    private final CarService carService;

    public UserController(SubscriptionService subscriptionService, CarService carService) {
        this.subscriptionService = subscriptionService;
        this.carService = carService;
    }

    @PostMapping("/user/add-car")
    public String addCar(@RequestParam String brand,
                         @RequestParam String model,
                         @RequestParam String licensePlate,
                         @RequestParam String color,
                         @RequestParam(required = false) MultipartFile document,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {

        AuthController.UserSessionView currentUser =
                (AuthController.UserSessionView) session.getAttribute("currentUser");

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in to add a car");
            return "redirect:/home";
        }

        try {
            carService.registerCar(
                    currentUser.getId(),
                    brand.trim(),
                    model.trim(),
                    licensePlate.trim().toUpperCase(),
                    color.trim(),
                    document
            );

            redirectAttributes.addFlashAttribute("success",
                    "Car " + brand + " " + model + " registered successfully!");

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to register car: " + e.getMessage());
        }

        return "redirect:/user";
    }

    @PostMapping("/user/delete-car/{carId}")
    public String deleteCar(@PathVariable Long carId,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {

        AuthController.UserSessionView currentUser =
                (AuthController.UserSessionView) session.getAttribute("currentUser");

        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please log in");
            return "redirect:/home";
        }

        try {
            carService.deleteCar(carId, currentUser.getId());
            redirectAttributes.addFlashAttribute("success", "Car removed successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Failed to remove car: " + e.getMessage());
        }

        return "redirect:/user";
    }

    @GetMapping("/user")
    public String userPage(HttpSession session, Model model) {
        AuthController.UserSessionView currentUser =
                (AuthController.UserSessionView) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/home";
        }

        model.addAttribute("user", currentUser);

        // Get user's cars from database
        List<Car> userCars = carService.getUserCars(currentUser.getId());
        List<CarView> cars = userCars.stream()
                .map(car -> new CarView(
                        car.getId(),
                        car.getBrand(),
                        car.getModel(),
                        car.getLicensePlate(),
                        car.getColor(),
                        car.getTimesParked()
                ))
                .collect(Collectors.toList());

        model.addAttribute("cars", cars);

        // Mock parking history (you can implement this later with actual data)
        List<ParkingHistoryView> lastParkings = List.of(
                new ParkingHistoryView("Campus Zuid", "Z-12",
                        OffsetDateTime.now().minusHours(3), 45),
                new ParkingHistoryView("Groenplaats Garage", "G-08",
                        OffsetDateTime.now().minusDays(1).minusHours(2), 90)
        );
        model.addAttribute("lastParkings", lastParkings);

        subscriptionService.getActiveSubscriptionForUser(currentUser.getId())
                .ifPresent(sub -> model.addAttribute("subscription", sub));

        return "user";
    }

    // Inner classes for views
    public static class CarView {
        private final Long id;
        private final String brand;
        private final String model;
        private final String licensePlate;
        private final String color;
        private final int timesParked;

        public CarView(Long id, String brand, String model, String licensePlate,
                       String color, int timesParked) {
            this.id = id;
            this.brand = brand;
            this.model = model;
            this.licensePlate = licensePlate;
            this.color = color;
            this.timesParked = timesParked;
        }

        public Long getId() { return id; }
        public String getBrand() { return brand; }
        public String getModel() { return model; }
        public String getLicensePlate() { return licensePlate; }
        public String getColor() { return color; }
        public int getTimesParked() { return timesParked; }
    }

    public static class ParkingHistoryView {
        private final String areaName;
        private final String spotLabel;
        private final OffsetDateTime parkedAt;
        private final int durationMinutes;

        public ParkingHistoryView(String areaName, String spotLabel,
                                  OffsetDateTime parkedAt, int durationMinutes) {
            this.areaName = areaName;
            this.spotLabel = spotLabel;
            this.parkedAt = parkedAt;
            this.durationMinutes = durationMinutes;
        }

        public String getAreaName() { return areaName; }
        public String getSpotLabel() { return spotLabel; }
        public OffsetDateTime getParkedAt() { return parkedAt; }
        public int getDurationMinutes() { return durationMinutes; }
    }
}