package product.dto.electronics;

import product.model.ProductType;

import java.math.BigDecimal;

public record ElectronicsDto(Long id,
                             String name,
                             BigDecimal basePrice,
                             Integer quantity,
                             ProductType productType) {
}
