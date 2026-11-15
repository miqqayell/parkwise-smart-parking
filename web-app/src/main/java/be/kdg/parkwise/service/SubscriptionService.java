package be.kdg.parkwise.service;

import be.kdg.parkwise.domain.Enum.SubscriptionType;
import be.kdg.parkwise.domain.SubscriptionPlan;
import be.kdg.parkwise.domain.UserSubscription;

import java.util.List;
import java.util.Optional;

/**
 * Service API for managing subscription plans and user subscriptions.
 *
 * Provides capability to browse active plans, subscribe/cancel/change a user's plan,
 * and to query effective access rights based on the current subscription.
 */
public interface SubscriptionService {

    /**
         * Retrieves all active subscription plans visible to end users.
         *
         * @return list of active plans
         */
        List<SubscriptionPlan> getActivePlans();

    UserSubscription subscribeUserToPlan(Long userId, Long planId);

    Optional<UserSubscription> getActiveSubscriptionForUser(Long userId);

    SubscriptionType getEffectivePlanCodeForUser(Long userId);

    boolean hasMapAccess(Long userId);

    boolean hasAnalyticsAccess(Long userId);

    boolean hasPredictionAccess(Long userId);

    UserSubscription cancelSubscription(Long userId);

    UserSubscription changeSubscription(Long userId, Long newPlanId);

    boolean canUpgradeTo(Long userId, String planCode);

    boolean canDowngradeTo(Long userId, String planCode);
}