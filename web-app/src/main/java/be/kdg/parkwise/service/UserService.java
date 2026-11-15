package be.kdg.parkwise.service;

import be.kdg.parkwise.domain.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findById(Long id);

    User registerUser(String email, String rawPassword, String firstName, String lastName);

    Optional<User> findByEmail(String email);

    boolean authenticateUser(String email, String rawPassword);

    boolean emailExists(String email);

    void updateLastLogin(String email);
}
