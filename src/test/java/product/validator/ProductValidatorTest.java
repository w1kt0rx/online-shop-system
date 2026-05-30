package product.validator;

import exception.InvalidProductException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductValidatorTest {

    @Test
    void shouldValidateProduct() {

        assertDoesNotThrow(
                () -> ProductValidator.validate(
                        1L,
                        "Laptop",
                        BigDecimal.valueOf(1000),
                        10
                )
        );
    }

    @Test
    void shouldThrowExceptionWhenIdIsInvalid() {

        assertThrows(
                InvalidProductException.class,
                () -> ProductValidator.validate(
                        null,
                        "Laptop",
                        BigDecimal.valueOf(1000),
                        10
                )
        );
    }

    @Test
    void shouldCollectMultipleValidationErrors() {

        InvalidProductException exception =
                assertThrows(
                        InvalidProductException.class,
                        () -> ProductValidator.validate(
                                null,
                                "",
                                BigDecimal.valueOf(-100),
                                -5
                        )
                );

        assertTrue(
                exception.getMessage()
                        .contains("Id cannot be null")
        );

        assertTrue(
                exception.getMessage()
                        .contains("Name cannot be blank")
        );

        assertTrue(
                exception.getMessage()
                        .contains("Price cannot be negative")
        );

        assertTrue(
                exception.getMessage()
                        .contains("Quantity cannot be negative")
        );
    }
}