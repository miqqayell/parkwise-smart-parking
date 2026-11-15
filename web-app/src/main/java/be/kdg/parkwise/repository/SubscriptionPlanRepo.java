package be.kdg.parkwise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import be.kdg.parkwise.domain.SubscriptionPlan;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepo extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findByActiveTrue();

    Optional<SubscriptionPlan> findByCode(String code);

}
