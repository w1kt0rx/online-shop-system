package product.mapper;

import product.dto.ElectronicsDto;
import product.model.electronics.Electronics;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ElectronicsMapper {

    public static ElectronicsDto toDTO(Electronics electronics) {
        return new ElectronicsDto(electronics.getId(),
                electronics.getName(),
                electronics.getBasePrice(),
                electronics.getQuantity(),
                electronics.getProductType());
    }

    public static Electronics toEntity(ElectronicsDto electronicsDto) {
        return new Electronics(electronicsDto.id(),
                electronicsDto.name(),
                electronicsDto.basePrice(),
                electronicsDto.quantity());
    }
}
