package product.dto;

import java.math.BigDecimal;

public record CreateElectronicsRequest(String name,
                                       BigDecimal basePrice,
                                       Integer quantity) {
}
