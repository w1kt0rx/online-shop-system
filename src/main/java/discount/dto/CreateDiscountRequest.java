package discount.dto;

import discount.model.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateDiscountRequest(
        String code,
        String description,
        DiscountType type,
        BigDecimal value,
        BigDecimal minOrderValue,
        LocalDateTime validFrom,
        LocalDateTime validTo
) {
}
