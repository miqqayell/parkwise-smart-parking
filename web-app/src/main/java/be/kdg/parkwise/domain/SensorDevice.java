package be.kdg.parkwise.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "sensors")
public class SensorDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sensor_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spot_id", nullable = false)
    private ParkingSpot spot;

    @Column(name = "sensor_type", nullable = false)
    private String sensorType;

    @Column(name = "hardware_model")
    private String hardwareModel;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // GETTERS & SETTERS
    public Long getId() { return id; }

    public ParkingSpot getSpot() { return spot; }
    public void setSpot(ParkingSpot spot) { this.spot = spot; }

    public String getSensorType() { return sensorType; }
    public void setSensorType(String sensorType) { this.sensorType = sensorType; }

    public String getHardwareModel() { return hardwareModel; }
    public void setHardwareModel(String hardwareModel) { this.hardwareModel = hardwareModel; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
