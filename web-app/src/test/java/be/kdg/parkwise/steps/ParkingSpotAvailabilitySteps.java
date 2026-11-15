package be.kdg.parkwise.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import be.kdg.parkwise.domain.*;
import be.kdg.parkwise.repository.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ParkingSpotAvailabilitySteps {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private ParkingAreaRepo parkingAreaRepo;
    @Autowired private ParkingSpotRepo parkingSpotRepo;
    @Autowired private SensorRepo sensorRepo;
    @Autowired private SensorReadingRepo sensorReadingRepo;
    @Autowired private SensorAlertRepo sensorAlertRepo;

    private ParkingArea area;
    private ParkingSpot spot;
    private SensorDevice sensor;

    private List<Map<String, Object>> response;

    // -------------------------
    // Background / Cleanup
    // -------------------------

    @Given("the parking database is clean")
    public void the_parking_database_is_clean() {
        // Delete in dependency order (alerts/readings -> sensors -> spots -> areas).
        // Alerts are created in the background by the sensor monitor.
        sensorAlertRepo.deleteAll();
        sensorReadingRepo.deleteAll();
        sensorRepo.deleteAll();
        parkingSpotRepo.deleteAll();
        parkingAreaRepo.deleteAll();

        // Make sure deletes are applied now
        sensorAlertRepo.flush();
        sensorReadingRepo.flush();
        sensorRepo.flush();
        parkingSpotRepo.flush();
        parkingAreaRepo.flush();
    }

    // -------------------------
    // Test data setup
    // -------------------------

    @Given("a parking spot exists with label {string} and spot number {int}")
    public void a_parking_spot_exists_with_label_and_spot_number(String label, int spotNumber) {
        area = new ParkingArea(label, 51.0, 4.0);
        area = parkingAreaRepo.saveAndFlush(area);

        spot = new ParkingSpot();
        spot.setArea(area);
        spot.setSpotNumber(spotNumber);
        spot.setOperating(true);
        spot = parkingSpotRepo.saveAndFlush(spot);

        assertThat(spot.getId()).isNotNull();
    }

    @Given("that spot has an active sensor")
    public void that_spot_has_an_active_sensor() {
        sensor = new SensorDevice();
        sensor.setSpot(spot);
        sensor.setSensorType("Magnetic + Distance");
        sensor.setActive(true);
        sensor = sensorRepo.saveAndFlush(sensor);

        assertThat(sensor.getId()).isNotNull();
    }

    @Given("that spot has no sensor")
    public void that_spot_has_no_sensor() {

    }

    @Given("the latest sensor reading indicates the spot is FREE")
    public void the_latest_sensor_reading_indicates_the_spot_is_free() {
        assertThat(sensor).as("Sensor must exist before creating readings").isNotNull();

        SensorReading reading = new SensorReading();
        reading.setSensor(sensor);
        reading.setReadingTime(Instant.parse("2025-01-01T10:00:00Z"));
        reading.setOccupied(false);

        sensorReadingRepo.saveAndFlush(reading);
    }

    @When("a newer sensor reading indicates the spot is OCCUPIED")
    public void a_newer_sensor_reading_indicates_the_spot_is_occupied() {
        assertThat(sensor).as("Sensor must exist before creating readings").isNotNull();

        SensorReading newer = new SensorReading();
        newer.setSensor(sensor);
        newer.setReadingTime(Instant.parse("2025-01-01T10:00:10Z")); // newer than previous
        newer.setOccupied(true);

        sensorReadingRepo.saveAndFlush(newer);
    }

    @Given("the sensor has no readings")
    public void the_sensor_has_no_readings() {

    }

    // -------------------------
    // API call + assertions
    // -------------------------

    @When("the driver requests the parking spot statuses")
    public void the_driver_requests_the_parking_spot_statuses() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/spots/status"))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        response = objectMapper.readValue(json, new TypeReference<>() {});
    }

    @Then("the response contains that spot with status {string}")
    public void the_response_contains_that_spot_with_status(String expectedStatus) {
        assertThat(response).isNotNull();
        assertThat(spot).isNotNull();

        Map<String, Object> row = response.stream()
                .filter(m -> {
                    Object spotIdObj = m.get("spotId");
                    if (spotIdObj == null) return false;
                    long spotId = ((Number) spotIdObj).longValue();
                    return spotId == spot.getId();
                })
                .findFirst()
                .orElse(null);

        assertThat(row)
                .as("Expected response to contain spotId=" + spot.getId() + " but got: " + response)
                .isNotNull();

        assertThat(row.get("status")).isEqualTo(expectedStatus);
    }
}
