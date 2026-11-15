package be.kdg.parkwise.dto;

import java.time.Instant;

public class SensorUpdateDto {
    private Long spotId;
    private boolean magneticTriggered;
    private double distanceCm;
    private Instant timestamp;

    public Long getSpotId() { return spotId; }
    public void setSpotId(Long spotId) { this.spotId = spotId; }

    public boolean isMagneticTriggered() { return magneticTriggered; }
    public void setMagneticTriggered(boolean magneticTriggered) { this.magneticTriggered = magneticTriggered; }

    public double getDistanceCm() { return distanceCm; }
    public void setDistanceCm(double distanceCm) { this.distanceCm = distanceCm; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
