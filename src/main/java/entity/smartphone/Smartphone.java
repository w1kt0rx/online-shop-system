package entity.smartphone;

import entity.Product;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
public class Smartphone extends Product {
    private final SmartphoneConfiguration configuration;

    public Smartphone(Long id, String name, BigDecimal basePrice, int quantity) {
        super(id, name, basePrice, quantity);
        configuration = new SmartphoneConfiguration();
    }

    @Override
    public BigDecimal getPrice() {
        return basePrice.add(configuration.calculateAdditionalPrice());
    }
}
