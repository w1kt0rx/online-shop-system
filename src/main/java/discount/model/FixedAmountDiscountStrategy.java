package discount.model;

import discount.strategy.DiscountStrategy;

import java.math.BigDecimal;

public class FixedAmountDiscountStrategy implements DiscountStrategy {
    private final BigDecimal amount;

    public FixedAmountDiscountStrategy(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Discount amount must be positive");
        }
        this.amount = amount;
    }

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
