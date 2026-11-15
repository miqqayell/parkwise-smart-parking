package be.kdg.parkwise.controller;

import be.kdg.parkwise.domain.ParkingSpot;
import be.kdg.parkwise.service.ParkingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class ParkingController {

    private final ParkingService parkingService;

    public ParkingController(ParkingService parkingService) {
        this.parkingService = parkingService;
    }

    @GetMapping("/api/spots")
    public List<Map<String, Object>> getAllSpots() {

        List<Map<String, Object>> result = new ArrayList<>();

        for (ParkingSpot spot : parkingService.findAll()) {

            Map<String, Object> obj = new HashMap<>();

            obj.put("id", spot.getId());
            obj.put("label", spot.getLabel());
            obj.put("latitude", spot.getLatitude());
            obj.put("longitude", spot.getLongitude());
            obj.put("status", spot.getStatus().toString());

            result.add(obj);
        }

        return result;
    }

    @GetMapping("/api/spots/status")
    public List<Map<String, Object>> getLatestStatuses() {

        List<Map<String, Object>> result = new ArrayList<>();

        for (ParkingSpot spot : parkingService.findAll()) {

            Map<String, Object> obj = new HashMap<>();
            obj.put("spotId", spot.getId());
            obj.put("status", parkingService.getLatestStatus(spot.getId()).toString());

            result.add(obj);
        }

        return result;
    }
}
