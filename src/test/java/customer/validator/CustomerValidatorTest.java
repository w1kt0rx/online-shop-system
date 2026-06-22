package customer.validator;

import exception.InvalidProductException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CustomerValidatorTest {

    @Test
    void shouldPassForValidData() {
        assertThatCode(() -> CustomerValidator.validate(1L, "Jan Kowalski", "wiktor@gmail.com", "Password!123"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 1L, 100L, Long.MAX_VALUE})
    void shouldPassForValidIds(long id) {
        assertThatCode(() -> CustomerValidator.validate(id, "Jan", "wiktor@gmail.com", "Password!123"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowWhenIdIsNull() {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> CustomerValidator.validate(null, "Jan", "wiktor@gmail.com", "Password!123"))
                .withMessageContaining("Customer id cannot be null or negative");
    }

    @Test
    void shouldThrowWhenIdIsNegative() {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> CustomerValidator.validate(-1L, "Jan", "wiktor@gmail.com", "Password!123"))
                .withMessageContaining("Customer id cannot be null or negative");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void shouldThrowWhenNameIsBlankOrNull(String name) {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> CustomerValidator.validate(1L, name, "wiktor@gmail.com", "Password!123"))
                .withMessageContaining("Customer name cannot be blank");
    }

    @Test
    void shouldCollectBothErrorsWhenBothInvalid() {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> CustomerValidator.validate(null, "", "wiktor@gmail.com", "Password!123"))
                .withMessageContaining("Customer id cannot be null or negative")
                .withMessageContaining("Customer name cannot be blank");
    }

    @Test
    void shouldPassValidateName() {
        assertThatCode(() -> CustomerValidator.validateName("Anna Nowak"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void shouldThrowValidateNameWhenBlankOrNull(String name) {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> CustomerValidator.validateName(name));
    }
}