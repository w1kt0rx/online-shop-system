package discount.dto;

import discount.model.DiscountType;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

public record CreateDiscountRequest(
        String code,
        String description,
        DiscountType type,
        BigDecimal value,
        BigDecimal minOrderValue,
        ZonedDateTime validFrom,
        ZonedDateTime validTo
) {
}
