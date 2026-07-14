package product.mapper.electronics;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import product.dto.electronics.ElectronicsDto;
import product.model.electronics.Electronics;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ElectronicsMapper {

    public static ElectronicsDto toDTO(Electronics electronics) {
        return new ElectronicsDto(
            electronics.getId(),
            electronics.getName(),
            electronics.getBasePrice(),
            electronics.getQuantity(),
            electronics.getProductType()
        );
    }

    public static Electronics toEntity(ElectronicsDto electronicsDto) {
        return new Electronics(
            electronicsDto.id(),
            electronicsDto.name(),
            electronicsDto.basePrice(),
            electronicsDto.quantity()
        );
    }
}
