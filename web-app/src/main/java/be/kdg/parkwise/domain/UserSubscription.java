package be.kdg.parkwise.domain;

import be.kdg.parkwise.domain.Enum.SubscriptionStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_subscription")
public class UserSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_subscription_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "locked_price", nullable = false)
    private BigDecimal lockedPrice;

    @Column(name = "locked_plan_name", nullable = false)
    private String lockedPlanName;

    @Column(name = "locked_plan_code", nullable = false)
    private String lockedPlanCode;

    @Column(name = "has_analytics", nullable = false)
    private boolean hasAnalytics;

    @Column(name = "has_prediction", nullable = false)
    private boolean hasPrediction;

    public UserSubscription() {
    }

    public UserSubscription(User user, SubscriptionPlan plan) {
        this.user = user;
        this.plan = plan;
        this.status = SubscriptionStatus.ACTIVE;
        this.startDate = LocalDateTime.now();
        this.lockedPrice = plan.getMonthlyPrice();
        this.lockedPlanName = plan.getName();
        this.lockedPlanCode = plan.getCode();
        this.hasAnalytics = plan.isHasAnalytics();
        this.hasPrediction = plan.isHasPrediction();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public SubscriptionPlan getPlan() {
        return plan;
    }

    public void setPlan(SubscriptionPlan plan) {
        this.plan = plan;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getLockedPrice() {
        return lockedPrice;
    }

    public void setLockedPrice(BigDecimal lockedPrice) {
        this.lockedPrice = lockedPrice;
    }

    public String getLockedPlanName() {
        return lockedPlanName;
    }

    public void setLockedPlanName(String lockedPlanName) {
        this.lockedPlanName = lockedPlanName;
    }

    public String getLockedPlanCode() {
        return lockedPlanCode;
    }

    public void setLockedPlanCode(String lockedPlanCode) {
        this.lockedPlanCode = lockedPlanCode;
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
        if (!(o instanceof UserSubscription)) return false;
        UserSubscription that = (UserSubscription) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}