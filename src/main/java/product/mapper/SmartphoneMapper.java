package product.mapper;

import product.dto.SmartphoneDto;
import product.model.smartphone.Smartphone;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SmartphoneMapper {
    public static SmartphoneDto toDTO(Smartphone smartphone) {
        return new SmartphoneDto(smartphone.getId(), smartphone.getName(), smartphone.getBasePrice(), smartphone.getQuantity(), smartphone.getProductType(), smartphone.getSmartphoneConfiguration());
    }

    public static Smartphone toEntity(SmartphoneDto smartphoneDto) {
        return new Smartphone(smartphoneDto.id(), smartphoneDto.name(), smartphoneDto.basePrice(), smartphoneDto.quantity(), smartphoneDto.smartphoneConfiguration());
    }
}
