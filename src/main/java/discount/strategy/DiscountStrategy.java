package discount.strategy;

import java.math.BigDecimal;

public interface DiscountStrategy {
    BigDecimal apply(BigDecimal originalPrice);
    String describe();
}
