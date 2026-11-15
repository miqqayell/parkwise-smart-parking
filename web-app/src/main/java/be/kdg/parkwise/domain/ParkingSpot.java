package be.kdg.parkwise.domain;

import be.kdg.parkwise.domain.valueobject.ParkingSpotStatus;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "parking_spots")
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "spot_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "area_id", nullable = false)
    private ParkingArea area;

    @Column(name = "spot_number", nullable = false)
    private Integer spotNumber;

    @Column(name = "is_operating", nullable = false)
    private boolean operating = true;

    // -----------------------
    // Transient fields for frontend
    // -----------------------
    @Transient
    private String label;

    @Transient
    private double latitude;

    @Transient
    private double longitude;

    @Transient
    private ParkingSpotStatus status = ParkingSpotStatus.UNKNOWN;

    @Transient
    private Instant lastUpdated = Instant.now();

    public ParkingSpot() { }

    @PostLoad
    private void fillTransientFields() {
        if (area != null) {
            this.label = area.getAreaName();
            this.latitude = area.getLatitude() != null ? area.getLatitude() : 0.0;
            this.longitude = area.getLongitude() != null ? area.getLongitude() : 0.0;
        }
    }

    // GETTERS / SETTERS

    public Long getId() { return id; }

    public ParkingArea getArea() { return area; }
    public void setArea(ParkingArea area) { this.area = area; }

    public Integer getSpotNumber() { return spotNumber; }
    public void setSpotNumber(Integer spotNumber) { this.spotNumber = spotNumber; }

    public boolean isOperating() { return operating; }
    public void setOperating(boolean operating) { this.operating = operating; }

    public String getLabel() { return label; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public ParkingSpotStatus getStatus() { return status; }
    public void setStatus(ParkingSpotStatus status) { this.status = status; }

    public Instant getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(Instant lastUpdated) { this.lastUpdated = lastUpdated; }
}
