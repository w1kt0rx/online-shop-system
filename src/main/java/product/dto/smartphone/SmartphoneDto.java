package product.dto.smartphone;

import product.dto.smartphone.configuration.SmartphoneConfigurationDto;
import product.model.ProductType;

import java.math.BigDecimal;

public record SmartphoneDto(Long id,
                            String name,
                            BigDecimal basePrice,
                            BigDecimal totalPrice,
                            Integer quantity,
                            ProductType productType,
                            SmartphoneConfigurationDto smartphoneConfiguration) {
}
