package discount.mapper;

import discount.dto.DiscountDto;
import discount.model.Discount;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DiscountMapper {
    public static DiscountDto toDto(Discount discount) {
        return new DiscountDto(
                discount.getId(),
                discount.getCode(),
                discount.getDescription(),
                discount.getType(),
                discount.getValue(),
                discount.getMinOrderValue(),
                discount.getValidFrom(),
                discount.getValidTo(),
                discount.isActive()
        );
    }
}
