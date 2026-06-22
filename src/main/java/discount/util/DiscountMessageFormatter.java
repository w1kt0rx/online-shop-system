package discount.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DiscountMessageFormatter {

    public static String applied(
            String discountCode,
            BigDecimal originalTotal,
            BigDecimal discountedTotal
    ) {
        return String.format(
                "[Discount] Applied '%s': %.2f zł → %.2f zł",
                discountCode,
                originalTotal.doubleValue(),
                discountedTotal.doubleValue()
        );
    }

    public static String failed(String discountCode, Exception e) {
        return String.format(
                "[Discount] Code '%s' could not be applied: %s",
                discountCode,
                e.getMessage()
        );
    }
}