package discount.model;

import discount.strategy.DiscountStrategy;
import discount.strategy.FixedAmountDiscountStrategy;
import discount.strategy.PercentageDiscountStrategy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Factory that creates the appropriate DiscountStrategy for a given Discount.
 * <p>
 * Uses a switch expression over DiscountType to instantiate the correct
 * implementation. This class is non-instantiable; all access is through the
 * static create(Discount) method.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DiscountFactoryStrategy {

    public static DiscountStrategy create(Discount discount) {
        return switch (discount.getType()) {
            case PERCENTAGE -> new PercentageDiscountStrategy(discount.getValue());
            case FIXED_AMOUNT -> new FixedAmountDiscountStrategy(discount.getValue());
        };
    }
}
