package product.dto.computer;

import product.dto.computer.configuration.ComputerConfigurationDto;
import product.model.ProductType;

import java.math.BigDecimal;

public record ComputerDto(Long id,
                          String name,
                          BigDecimal basePrice,
                          BigDecimal totalPrice,
                          Integer quantity,
                          ProductType productType,
                          ComputerConfigurationDto computerConfiguration) {
}
