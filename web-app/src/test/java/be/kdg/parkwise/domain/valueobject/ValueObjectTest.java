package be.kdg.parkwise.domain.valueobject;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValueObjectTest {

    @Test
    void validEmailIsAccepted() {
        assertThat(new Email("driver@example.com").toString()).isEqualTo("driver@example.com");
    }

    @Test
    void malformedOrEmptyEmailIsRejected() {
        assertThatThrownBy(() -> new Email("not-an-email")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Email("missing@tld")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new Email(" ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rawPasswordMustHaveAtLeastSixCharacters() {
        Password.validateRawPassword("secret");
        assertThatThrownBy(() -> Password.validateRawPassword("12345"))
                .hasMessageContaining("at least 6");
        assertThatThrownBy(() -> Password.validateRawPassword(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void storedPasswordHashCannotBeEmpty() {
        assertThatThrownBy(() -> new Password("")).isInstanceOf(IllegalArgumentException.class);
    }
}
