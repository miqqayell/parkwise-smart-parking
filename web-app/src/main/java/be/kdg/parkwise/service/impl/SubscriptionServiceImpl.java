package be.kdg.parkwise.service.impl;

import be.kdg.parkwise.domain.SubscriptionPlan;
import be.kdg.parkwise.domain.User;
import be.kdg.parkwise.domain.UserSubscription;
import be.kdg.parkwise.domain.Enum.SubscriptionType;
import be.kdg.parkwise.domain.Enum.SubscriptionStatus;
import be.kdg.parkwise.repository.SubscriptionPlanRepo;
import be.kdg.parkwise.repository.UserRepository;
import be.kdg.parkwise.repository.UserSubscriptionRepository;
import be.kdg.parkwise.service.SubscriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Default {@link be.kdg.parkwise.service.SubscriptionService} implementation.
 *
 * Orchestrates subscription plan lookup and user subscription lifecycle (subscribe, cancel, change),
 * and exposes convenience checks for feature access. Uses Spring Data repositories for persistence
 * and marks mutating operations as transactional.
 */
@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionPlanRepo subscriptionPlanRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionServiceImpl(SubscriptionPlanRepo subscriptionPlanRepository,
                                   UserSubscriptionRepository userSubscriptionRepository,
                                   UserRepository userRepository) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<SubscriptionPlan> getActivePlans() {
        return subscriptionPlanRepository.findByActiveTrue();
    }

    @Override
    @Transactional
    public UserSubscription subscribeUserToPlan(Long userId, Long planId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        if (!plan.isActive()) {
            throw new IllegalArgumentException("This plan is no longer available");
        }

        Optional<UserSubscription> existingActive = userSubscriptionRepository
                .findFirstByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);

        if (existingActive.isPresent()) {
            throw new IllegalArgumentException("User already has an active subscription");
        }

        UserSubscription subscription = new UserSubscription(user, plan);
        return userSubscriptionRepository.save(subscription);
    }

    @Override
    public Optional<UserSubscription> getActiveSubscriptionForUser(Long userId) {
        return userSubscriptionRepository.findFirstByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE);
    }

    @Override
    public SubscriptionType getEffectivePlanCodeForUser(Long userId) {
        Optional<UserSubscription> subscription = getActiveSubscriptionForUser(userId);

        if (subscription.isEmpty()) {
            return null;
        }

        String code = subscription.get().getLockedPlanCode();
        try {
            return SubscriptionType.valueOf(code);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public boolean hasMapAccess(Long userId) {
        return getActiveSubscriptionForUser(userId).isPresent();
    }

    @Override
    public boolean hasAnalyticsAccess(Long userId) {
        Optional<UserSubscription> subscription = getActiveSubscriptionForUser(userId);
        return subscription.isPresent() && subscription.get().isHasAnalytics();
    }

    @Override
    public boolean hasPredictionAccess(Long userId) {
        Optional<UserSubscription> subscription = getActiveSubscriptionForUser(userId);
        return subscription.isPresent() && subscription.get().isHasPrediction();
    }

    @Override
    @Transactional
    public UserSubscription cancelSubscription(Long userId) {
        UserSubscription activeSubscription = getActiveSubscriptionForUser(userId).orElseThrow(() ->   new IllegalArgumentException("User has no active subscription"));

        activeSubscription.setStatus(SubscriptionStatus.CANCELLED);
        activeSubscription.setEndDate(LocalDateTime.now());

        return userSubscriptionRepository.save(activeSubscription);
    }

    @Override
    @Transactional
    public UserSubscription changeSubscription(Long userId, Long newPlanId ){
        UserSubscription oldSubscription = getActiveSubscriptionForUser(userId).orElseThrow(() -> new IllegalArgumentException("User has no active subscription"));

        oldSubscription.setStatus(SubscriptionStatus.CANCELLED);
        oldSubscription.setEndDate(LocalDateTime.now());
        userSubscriptionRepository.save(oldSubscription);

        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        SubscriptionPlan newPlan = subscriptionPlanRepository.findById(newPlanId).orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        if (!newPlan.isActive()) {
            throw new IllegalArgumentException("This plan is no longer available");
        }
        UserSubscription newSubscription = new UserSubscription(user, newPlan);
        return userSubscriptionRepository.save(newSubscription);
    }

    @Override
    public boolean canUpgradeTo(Long userId, String planCode) {
        Optional<UserSubscription> currentSub = getActiveSubscriptionForUser(userId);

        if(currentSub.isEmpty()){
            return true; //Can subscribe to anything
        }

        String currentPlanCode= currentSub.get().getLockedPlanCode();

        // BASIC can upgrade to PREMIUM or ULTRA
        if ("BASIC".equals(currentPlanCode)) {
            return "PREMIUM".equals(planCode) || "ULTRA".equals(planCode);
        }

        // PREMIUM can upgrade to ULTRA
        if ("PREMIUM".equals(currentPlanCode)) {
            return "ULTRA".equals(planCode);
        }

        // ULTRA is the highest, cannot upgrade
        return false;
    }

    @Override
    public boolean canDowngradeTo(Long userId, String planCode) {
        Optional<UserSubscription> currentSub = getActiveSubscriptionForUser(userId);
        if (currentSub.isEmpty()) {
            return false; // nothing to downgrade
        }

        String currentPlanCode = currentSub.get().getLockedPlanCode();

        // ULTRA can downgrade to PREMIUM or BASIC
        if ("ULTRA".equals(currentPlanCode)) {
            return "PREMIUM".equals(planCode) || "BASIC".equals(planCode);
        }

        // PREMIUM can downgrade to BASIC
        if ("PREMIUM".equals(currentPlanCode)) {
            return "BASIC".equals(planCode);
        }

        // BASIC is the lowest, cannot downgrade
        return false;
    }

}