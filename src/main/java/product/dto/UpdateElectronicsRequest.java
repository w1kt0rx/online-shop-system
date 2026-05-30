package product.dto;

import java.math.BigDecimal;

public record UpdateElectronicsRequest(String name,
                                       BigDecimal basePrice,
                                       Integer quantity) {
}