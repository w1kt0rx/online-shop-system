package discount.validator;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import discount.dto.CreateDiscountRequest;
import discount.model.DiscountType;
import exception.InvalidProductException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;

class DiscountValidatorTest {

    private CreateDiscountRequest validRequest() {
        return new CreateDiscountRequest(
            "SUMMER10",
            "Summer sale",
            DiscountType.PERCENTAGE,
            new BigDecimal("10"),
            null,
            ZonedDateTime.now().minusHours(1),
            ZonedDateTime.now().plusDays(7)
        );
    }

    @Test
    void shouldPassForValidRequest() {
        assertThatCode(() -> DiscountValidator.validate(validRequest())).doesNotThrowAnyException();
    }

    @Test
    void shouldThrowWhenCodeIsNull() {
        CreateDiscountRequest request = new CreateDiscountRequest(
            null,
            "desc",
            DiscountType.PERCENTAGE,
            new BigDecimal("10"),
            null,
            ZonedDateTime.now().minusHours(1),
            ZonedDateTime.now().plusDays(1)
        );
        assertThatThrownBy(() -> DiscountValidator.validate(request))
            .isInstanceOf(InvalidProductException.class)
            .hasMessageContaining("code");
    }

    @Test
    void shouldThrowWhenCodeIsBlank() {
        CreateDiscountRequest request = new CreateDiscountRequest(
            "   ",
            "desc",
            DiscountType.PERCENTAGE,
            new BigDecimal("10"),
            null,
            ZonedDateTime.now().minusHours(1),
            ZonedDateTime.now().plusDays(1)
        );
        assertThatThrownBy(() -> DiscountValidator.validate(request))
            .isInstanceOf(InvalidProductException.class)
            .hasMessageContaining("code");
    }

    @Test
    void shouldThrowWhenValueIsNull() {
        CreateDiscountRequest request = new CreateDiscountRequest(
            "CODE10",
            "desc",
            DiscountType.PERCENTAGE,
            null,
            null,
            ZonedDateTime.now().minusHours(1),
            ZonedDateTime.now().plusDays(1)
        );
        assertThatThrownBy(() -> DiscountValidator.validate(request))
            .isInstanceOf(InvalidProductException.class)
            .hasMessageContaining("value");
    }

    @Test
    void shouldThrowWhenValueIsZero() {
        CreateDiscountRequest request = new CreateDiscountRequest(
            "CODE10",
            "desc",
            DiscountType.PERCENTAGE,
            BigDecimal.ZERO,
            null,
            ZonedDateTime.now().minusHours(1),
            ZonedDateTime.now().plusDays(1)
        );
        assertThatThrownBy(() -> DiscountValidator.validate(request))
            .isInstanceOf(InvalidProductException.class)
            .hasMessageContaining("value");
    }

    @Test
    void shouldThrowWhenValueIsNegative() {
        CreateDiscountRequest request = new CreateDiscountRequest(
            "CODE10",
            "desc",
            DiscountType.PERCENTAGE,
            new BigDecimal("-5"),
            null,
            ZonedDateTime.now().minusHours(1),
            ZonedDateTime.now().plusDays(1)
        );
        assertThatThrownBy(() -> DiscountValidator.validate(request)).isInstanceOf(InvalidProductException.class);
    }

    @Test
    void shouldThrowWhenValidFromIsNull() {
        CreateDiscountRequest request = new CreateDiscountRequest(
            "CODE10",
            "desc",
            DiscountType.PERCENTAGE,
            new BigDecimal("10"),
            null,
            null,
            ZonedDateTime.now().plusDays(1)
        );
        assertThatThrownBy(() -> DiscountValidator.validate(request))
            .isInstanceOf(InvalidProductException.class)
            .hasMessageContaining("date");
    }

    @Test
    void shouldThrowWhenValidToIsNull() {
        CreateDiscountRequest request = new CreateDiscountRequest(
            "CODE10",
            "desc",
            DiscountType.PERCENTAGE,
            new BigDecimal("10"),
            null,
            ZonedDateTime.now().minusHours(1),
            null
        );
        assertThatThrownBy(() -> DiscountValidator.validate(request))
            .isInstanceOf(InvalidProductException.class)
            .hasMessageContaining("date");
    }

    @Test
    void shouldThrowWhenValidToIsBeforeValidFrom() {
        CreateDiscountRequest request = new CreateDiscountRequest(
            "CODE10",
            "desc",
            DiscountType.PERCENTAGE,
            new BigDecimal("10"),
            null,
            ZonedDateTime.now().plusDays(5),
            ZonedDateTime.now().plusDays(1)
        );
        assertThatThrownBy(() -> DiscountValidator.validate(request))
            .isInstanceOf(InvalidProductException.class)
            .hasMessageContaining("validTo");
    }
}
