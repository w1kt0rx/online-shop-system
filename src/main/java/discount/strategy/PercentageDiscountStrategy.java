package discount.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DiscountStrategy that reduces a price by a given percentage.
 * The discount amount is calculated as originalPrice * percentage / 100,
 * rounded to two decimal places using RoundingMode.HALF_UP.
 */
public class PercentageDiscountStrategy implements DiscountStrategy {

    private final BigDecimal percentage;

    /**
     * Creates a percentage-based discount strategy.
     *
     * @param percentage the percentage to deduct (e.g. 10 for 10%);
     *                   must be between 0 and 100 inclusive
     * @throws IllegalArgumentException if the percentage is outside the [0, 100] range
     */
    public PercentageDiscountStrategy(BigDecimal percentage) {
        if (percentage.compareTo(BigDecimal.ZERO) < 0 || percentage.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentage must be between 0 and 100");
        }
        this.percentage = percentage;
    }

    /**
     * Computes originalPrice - (originalPrice * percentage / 100).
     */
    @Override
    public BigDecimal apply(BigDecimal originalPrice) {
        BigDecimal discount = originalPrice.multiply(percentage).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return originalPrice.subtract(discount);
    }

    @Override
    public String describe() {
        return percentage.toString() + "% discount";
    }
}
