package product.dto.smartphone;

import java.math.BigDecimal;
import product.dto.smartphone.configuration.SmartphoneConfigurationDto;
import product.model.ProductType;

public record SmartphoneDto(
    Long id,
    String name,
    BigDecimal basePrice,
    BigDecimal totalPrice,
    Integer quantity,
    ProductType productType,
    SmartphoneConfigurationDto smartphoneConfiguration
) {}
