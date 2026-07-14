package product.dto.computer;

import java.math.BigDecimal;
import product.dto.computer.configuration.ComputerConfigurationDto;
import product.model.ProductType;

public record ComputerDto(
    Long id,
    String name,
    BigDecimal basePrice,
    BigDecimal totalPrice,
    Integer quantity,
    ProductType productType,
    ComputerConfigurationDto computerConfiguration
) {}
