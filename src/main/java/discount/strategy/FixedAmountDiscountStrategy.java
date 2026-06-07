package discount.strategy;

import java.math.BigDecimal;

/**
 * DiscountStrategy that subtracts a fixed monetary amount from a price.
 * If the deduction would result in a negative price, the returned amount is
 * clamped to BigDecimal.ZERO.
 */
public class FixedAmountDiscountStrategy implements DiscountStrategy {
    private final BigDecimal amount;

    /**
     * Creates a fixed-amount discount strategy.
     *
     * @param amount the amount to subtract; must be positive (greater than zero)
     * @throws IllegalArgumentException if amount is zero or negative
     */
    public FixedAmountDiscountStrategy(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount amount must be positive");
        }
        this.amount = amount;
    }

    /**
     * max(originalPrice - amount, 0).
     */
    @Override
    public BigDecimal apply(BigDecimal originalPrice) {
        BigDecimal result = originalPrice.subtract(amount);
        return result.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : result;
    }

    @Override
    public String describe() {
        return amount.toString() + " pln discount";
    }
}
