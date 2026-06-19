package discount.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountTest {

    private Discount activeDiscount(ZonedDateTime from, ZonedDateTime to) {
        return new Discount(1L, "CODE", "desc", DiscountType.PERCENTAGE,
                new BigDecimal("10"), null, from, to);
    }

    @Test
    void shouldBeValidWhenActiveAndWithinDateRange() {
        Discount discount = activeDiscount(
                ZonedDateTime.now().minusDays(1),
                ZonedDateTime.now().plusDays(1)
        );
        assertThat(discount.isValid()).isTrue();
    }

    @Test
    void shouldBeInvalidWhenDeactivated() {
        Discount discount = activeDiscount(
                ZonedDateTime.now().minusDays(1),
                ZonedDateTime.now().plusDays(1)
        );
        discount.deactivate();
        assertThat(discount.isValid()).isFalse();
    }

    @Test
    void shouldBeInvalidWhenValidFromIsInFuture() {
        Discount discount = activeDiscount(
                ZonedDateTime.now().plusDays(1),
                ZonedDateTime.now().plusDays(5)
        );
        assertThat(discount.isValid()).isFalse();
    }

    @Test
    void shouldBeInvalidWhenValidToIsInPast() {
        Discount discount = activeDiscount(
                ZonedDateTime.now().minusDays(5),
                ZonedDateTime.now().minusDays(1)
        );
        assertThat(discount.isValid()).isFalse();
    }

    @Test
    void shouldBeActiveByDefault() {
        Discount discount = activeDiscount(
                ZonedDateTime.now().minusDays(1),
                ZonedDateTime.now().plusDays(1)
        );
        assertThat(discount.isActive()).isTrue();
    }

    @Test
    void shouldSetActiveFalseAfterDeActivate() {
        Discount discount = activeDiscount(
                ZonedDateTime.now().minusDays(1),
                ZonedDateTime.now().plusDays(1)
        );
        discount.deactivate();
        assertThat(discount.isActive()).isFalse();
    }

    @Test
    void shouldBeInvalidWhenDeactivatedEvenIfDatesAreValid() {
        Discount discount = activeDiscount(
                ZonedDateTime.now().minusHours(1),
                ZonedDateTime.now().plusHours(1)
        );
        discount.deactivate();
        assertThat(discount.isValid()).isFalse();
    }
}