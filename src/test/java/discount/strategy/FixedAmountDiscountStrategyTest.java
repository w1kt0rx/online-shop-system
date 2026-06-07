package discount.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FixedAmountDiscountStrategyTest {

    @Test
    void shouldSubtractFixedAmountFromPrice() {
        FixedAmountDiscountStrategy strategy = new FixedAmountDiscountStrategy(new BigDecimal("100"));
        BigDecimal result = strategy.apply(new BigDecimal("500.00"));
        assertThat(result).isEqualByComparingTo(new BigDecimal("400.00"));
    }

    @Test
    void shouldReturnZeroWhenDiscountExceedsOrderTotal() {
        FixedAmountDiscountStrategy strategy = new FixedAmountDiscountStrategy(new BigDecimal("600"));
        BigDecimal result = strategy.apply(new BigDecimal("500.00"));
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldReturnZeroWhenDiscountExactlyEqualsOrderTotal() {
        FixedAmountDiscountStrategy strategy = new FixedAmountDiscountStrategy(new BigDecimal("500"));
        BigDecimal result = strategy.apply(new BigDecimal("500.00"));
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldDescribeFixedAmountDiscount() {
        FixedAmountDiscountStrategy strategy = new FixedAmountDiscountStrategy(new BigDecimal("50"));
        assertThat(strategy.describe()).contains("50").contains("pln");
    }

    @Test
    void shouldThrowWhenAmountIsZero() {
        assertThatThrownBy(() -> new FixedAmountDiscountStrategy(BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowWhenAmountIsNegative() {
        assertThatThrownBy(() -> new FixedAmountDiscountStrategy(new BigDecimal("-10")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}