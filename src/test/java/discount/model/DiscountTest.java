package discount.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class DiscountTest {

    private Discount activeDiscount(LocalDateTime from, LocalDateTime to) {
        return new Discount(1L, "CODE", "desc", DiscountType.PERCENTAGE,
                new BigDecimal("10"), null, from, to);
    }

    @Test
    void shouldBeValidWhenActiveAndWithinDateRange() {
        Discount discount = activeDiscount(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );
        assertThat(discount.isValid()).isTrue();
    }

    @Test
    void shouldBeInvalidWhenDeactivated() {
        Discount discount = activeDiscount(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );
        discount.deActivate();
        assertThat(discount.isValid()).isFalse();
    }

    @Test
    void shouldBeInvalidWhenValidFromIsInFuture() {
        Discount discount = activeDiscount(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(5)
        );
        assertThat(discount.isValid()).isFalse();
    }

    @Test
    void shouldBeInvalidWhenValidToIsInPast() {
        Discount discount = activeDiscount(
                LocalDateTime.now().minusDays(5),
                LocalDateTime.now().minusDays(1)
        );
        assertThat(discount.isValid()).isFalse();
    }

    @Test
    void shouldBeActiveByDefault() {
        Discount discount = activeDiscount(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );
        assertThat(discount.isActive()).isTrue();
    }

    @Test
    void shouldSetActiveFalseAfterDeActivate() {
        Discount discount = activeDiscount(
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1)
        );
        discount.deActivate();
        assertThat(discount.isActive()).isFalse();
    }

    @Test
    void shouldBeInvalidWhenDeactivatedEvenIfDatesAreValid() {
        Discount discount = activeDiscount(
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now().plusHours(1)
        );
        discount.deActivate();
        assertThat(discount.isValid()).isFalse();
    }
}