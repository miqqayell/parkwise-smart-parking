package be.kdg.parkwise.repository;

import be.kdg.parkwise.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Finds all users with a specific role.
     * Used to retrieve system administrators for notification delivery.
     *
     * @param role the role to filter by (e.g., "system_admin", "user")
     * @return list of users with the specified role
     */
    List<User> findByRole(String role);
}
