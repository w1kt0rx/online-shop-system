package product.dto;

import product.model.ProductType;
import product.model.computer.configuration.ComputerConfiguration;

import java.math.BigDecimal;

public record ComputerDto(Long id,
                          String name,
                          BigDecimal basePrice,
                          BigDecimal totalPrice,
                          Integer quantity,
                          ProductType productType,
                          ComputerConfigurationDto computerConfiguration) {
}
