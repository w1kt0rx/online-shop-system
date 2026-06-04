package discount.model;

import discount.strategy.DiscountStrategy;
import discount.strategy.PercentageDiscountStrategy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DiscountFactoryStrategy {

    public static DiscountStrategy create(Discount discount) {
        return switch (discount.getType()) {
            case PERCENTAGE -> new PercentageDiscountStrategy(discount.getValue());
            case FIXED_AMOUNT -> new FixedAmountDiscountStrategy(discount.getValue());
        };
    }
}
