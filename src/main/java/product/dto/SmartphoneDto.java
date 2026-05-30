package product.dto;

import product.model.ProductType;
import product.model.smartphone.configuration.SmartphoneConfiguration;

import java.math.BigDecimal;

public record SmartphoneDto(Long id,
                            String name,
                            BigDecimal basePrice,
                            BigDecimal totalPrice,
                            Integer quantity,
                            ProductType productType,
                            SmartphoneConfigurationDto smartphoneConfiguration) {
}
