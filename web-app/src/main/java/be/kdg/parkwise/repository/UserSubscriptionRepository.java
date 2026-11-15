package be.kdg.parkwise.repository;

import be.kdg.parkwise.domain.UserSubscription;
import be.kdg.parkwise.domain.Enum.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {

    List<UserSubscription> findByUserId(Long userId);

    Optional<UserSubscription> findFirstByUserIdAndStatus(Long userId, SubscriptionStatus status);
}