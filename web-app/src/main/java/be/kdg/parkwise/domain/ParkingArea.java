package be.kdg.parkwise.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "parking_areas")
public class ParkingArea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "area_id")
    private Long id;

    @Column(name = "area_name", nullable = false)
    private String areaName;

    @Column(name = "area_postcode")
    private String areaPostcode;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "address")
    private String address;

    @Column(name = "total_capacity")
    private Integer totalCapacity;

    @Column(name = "area_type")
    private String areaType;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public ParkingArea() {}

    public ParkingArea(String name, Double lat, Double lon) {
        this.areaName = name;
        this.latitude = lat;
        this.longitude = lon;
    }

    // GETTERS & SETTERS
    public Long getId() { return id; }

    public String getAreaName() { return areaName; }
    public void setAreaName(String areaName) { this.areaName = areaName; }

    public String getAreaPostcode() { return areaPostcode; }
    public void setAreaPostcode(String areaPostcode) { this.areaPostcode = areaPostcode; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Integer getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(Integer totalCapacity) { this.totalCapacity = totalCapacity; }

    public String getAreaType() { return areaType; }
    public void setAreaType(String areaType) { this.areaType = areaType; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
