package be.kdg.parkwise.domain.valueobject;

public record Password(String hashedValue) {

    public Password {
        if (hashedValue == null || hashedValue.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
    }

    public static void validateRawPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
    }
}
