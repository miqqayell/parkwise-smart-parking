package be.kdg.parkwise.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long id;

    @Column(name = "plan_name", nullable = false)
    private String name;

    @Column(name = "plan_code", nullable = false, unique = true)
    private String code;

    @Column(name = "monthly_price", nullable = false)
    private BigDecimal monthlyPrice;

    @Column(name = "description")
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "has_analytics", nullable = false)
    private boolean hasAnalytics = false;

    @Column(name = "has_prediction", nullable = false)
    private boolean hasPrediction = false;

    public SubscriptionPlan() {
    }

    public SubscriptionPlan(String name, String code, BigDecimal monthlyPrice,
                            String description, boolean hasAnalytics, boolean hasPrediction) {
        this.name = name;
        this.code = code;
        this.monthlyPrice = monthlyPrice;
        this.description = description;
        this.hasAnalytics = hasAnalytics;
        this.hasPrediction = hasPrediction;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getMonthlyPrice() {
        return monthlyPrice;
    }

    public void setMonthlyPrice(BigDecimal monthlyPrice) {
        this.monthlyPrice = monthlyPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isHasAnalytics() {
        return hasAnalytics;
    }

    public void setHasAnalytics(boolean hasAnalytics) {
        this.hasAnalytics = hasAnalytics;
    }

    public boolean isHasPrediction() {
        return hasPrediction;
    }

    public void setHasPrediction(boolean hasPrediction) {
        this.hasPrediction = hasPrediction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubscriptionPlan)) return false;
        SubscriptionPlan that = (SubscriptionPlan) o;
        return Objects.equals(id, that.id) && Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, code);
    }
}