package product.dto.electronics;

import java.math.BigDecimal;
import product.model.ProductType;

public record ElectronicsDto(Long id, String name, BigDecimal basePrice, Integer quantity, ProductType productType) {}
