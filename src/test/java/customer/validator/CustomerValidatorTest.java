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
        assertThatCode(() -> CustomerValidator.validate(1L, "Jan Kowalski"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, 1L, 100L, Long.MAX_VALUE})
    void shouldPassForValidIds(long id) {
        assertThatCode(() -> CustomerValidator.validate(id, "Jan"))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowWhenIdIsNull() {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> CustomerValidator.validate(null, "Jan"))
                .withMessageContaining("Customer id cannot be null or negative");
    }

    @Test
    void shouldThrowWhenIdIsNegative() {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> CustomerValidator.validate(-1L, "Jan"))
                .withMessageContaining("Customer id cannot be null or negative");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    void shouldThrowWhenNameIsBlankOrNull(String name) {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> CustomerValidator.validate(1L, name))
                .withMessageContaining("Customer name cannot be blank");
    }

    @Test
    void shouldCollectBothErrorsWhenBothInvalid() {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> CustomerValidator.validate(null, ""))
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