package be.kdg.parkwise.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sensor_readings")
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reading_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private SensorDevice sensor;

    @Column(name = "reading_time")
    private Instant readingTime;

    @Column(name = "distance_mm")
    private Integer distanceMm;

    @Column(name = "metal_detected")
    private Boolean metalDetected;

    @Column(name = "occupied")
    private Boolean occupied;

    // GETTERS & SETTERS
    public Long getId() { return id; }
    public SensorDevice getSensor() { return sensor; }
    public void setSensor(SensorDevice sensor) { this.sensor = sensor; }

    public Instant getReadingTime() { return readingTime; }
    public void setReadingTime(Instant readingTime) { this.readingTime = readingTime; }

    public Integer getDistanceMm() { return distanceMm; }
    public void setDistanceMm(Integer distanceMm) { this.distanceMm = distanceMm; }

    public Boolean getMetalDetected() { return metalDetected; }
    public void setMetalDetected(Boolean metalDetected) { this.metalDetected = metalDetected; }

    public Boolean getOccupied() { return occupied; }
    public void setOccupied(Boolean occupied) { this.occupied = occupied; }
}
