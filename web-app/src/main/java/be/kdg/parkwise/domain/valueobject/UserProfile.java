package be.kdg.parkwise.domain.valueobject;

public record UserProfile(String firstName, String lastName) {

    public UserProfile {
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be empty");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be empty");
        }
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
