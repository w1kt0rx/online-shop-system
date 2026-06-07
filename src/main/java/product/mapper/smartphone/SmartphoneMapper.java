package product.mapper.smartphone;

import product.dto.smartphone.SmartphoneDto;
import product.mapper.smartphone.configuration.SmartphoneConfigurationMapper;
import product.model.smartphone.Smartphone;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SmartphoneMapper {
    public static SmartphoneDto toDTO(Smartphone smartphone) {
        return new SmartphoneDto(smartphone.getId(),
                smartphone.getName(),
                smartphone.getBasePrice(),
                smartphone.getPrice(),
                smartphone.getQuantity(),
                smartphone.getProductType(),
                SmartphoneConfigurationMapper.toDto(smartphone.getSmartphoneConfiguration()));
    }

    public static Smartphone toEntity(SmartphoneDto smartphoneDto) {
        return new Smartphone(smartphoneDto.id(),
                smartphoneDto.name(),
                smartphoneDto.basePrice(),
                smartphoneDto.quantity(),
                SmartphoneConfigurationMapper.toEntity(smartphoneDto.smartphoneConfiguration()));
    }
}
