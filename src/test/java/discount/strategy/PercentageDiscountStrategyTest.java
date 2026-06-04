package discount.strategy;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PercentageDiscountStrategyTest {

    @Test
    void shouldApplyTenPercentDiscount() {
        PercentageDiscountStrategy strategy = new PercentageDiscountStrategy(new BigDecimal("10"));
        BigDecimal result = strategy.apply(new BigDecimal("200.00"));
        assertThat(result).isEqualByComparingTo(new BigDecimal("180.00"));
    }

    @Test
    void shouldApplyFiftyPercentDiscount() {
        PercentageDiscountStrategy strategy = new PercentageDiscountStrategy(new BigDecimal("50"));
        BigDecimal result = strategy.apply(new BigDecimal("1000.00"));
        assertThat(result).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void shouldApplyHundredPercentDiscountResultingInZero() {
        PercentageDiscountStrategy strategy = new PercentageDiscountStrategy(new BigDecimal("100"));
        BigDecimal result = strategy.apply(new BigDecimal("300.00"));
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldApplyZeroPercentDiscountLeavingPriceUnchanged() {
        PercentageDiscountStrategy strategy = new PercentageDiscountStrategy(new BigDecimal("0"));
        BigDecimal result = strategy.apply(new BigDecimal("500.00"));
        assertThat(result).isEqualByComparingTo(new BigDecimal("500.00"));
    }

    @Test
    void shouldRoundResultToTwoDecimalPlaces() {
        PercentageDiscountStrategy strategy = new PercentageDiscountStrategy(new BigDecimal("10"));
        BigDecimal result = strategy.apply(new BigDecimal("333.33"));
        // 333.33 * 10% = 33.333, rounded to 33.33, result = 299.99 (not 299.997)
        assertThat(result).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    void shouldDescribePercentageDiscount() {
        PercentageDiscountStrategy strategy = new PercentageDiscountStrategy(new BigDecimal("15"));
        assertThat(strategy.describe()).contains("15").contains("%");
    }

    @Test
    void shouldThrowWhenPercentageIsNegative() {
        assertThatThrownBy(() -> new PercentageDiscountStrategy(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldThrowWhenPercentageExceedsHundred() {
        assertThatThrownBy(() -> new PercentageDiscountStrategy(new BigDecimal("101")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}