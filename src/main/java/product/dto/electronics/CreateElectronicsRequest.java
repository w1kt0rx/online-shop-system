package product.dto.electronics;

import java.math.BigDecimal;

public record CreateElectronicsRequest(String name, BigDecimal basePrice, Integer quantity) {
    public static CreateElectronicsRequest of(String name, BigDecimal basePrice, Integer quantity) {
        return new CreateElectronicsRequest(name, basePrice, quantity);
    }
}
