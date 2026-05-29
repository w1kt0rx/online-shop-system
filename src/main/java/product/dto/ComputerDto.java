package product.dto;

import product.model.ProductType;
import product.model.computer.configuration.ComputerConfiguration;

import java.math.BigDecimal;

public record ComputerDto(Long id,
                          String name,
                          BigDecimal basePrice,
                          Integer quantity,
                          ProductType productType,
                          ComputerConfiguration computerConfiguration) {
}
