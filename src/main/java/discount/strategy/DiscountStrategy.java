package discount.strategy;

import java.math.BigDecimal;

/**
 * Strategy interface for applying a discount to a given price.
 * Implementations define specific calculation logic (e.g., percentage-based
 * or fixed-amount deduction).
 */
public interface DiscountStrategy {
    /**
     * Calculates and returns the price after applying the discount.
     *
     * @param originalPrice the price before the discount; must not be null
     * @return the discounted price; guaranteed to be >= 0
     */
    BigDecimal apply(BigDecimal originalPrice);

    /**
     * Returns a short, human-readable description of the discount (e.g. "10% discount").
     *
     * @return non-null description string
     */
    String describe();
}
