package discount.dto;

import discount.model.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record DiscountDto(
        Long id,
        String code,
        String description,
        DiscountType type,
        BigDecimal value,
        BigDecimal minOrderValue,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        boolean active
) {
}
