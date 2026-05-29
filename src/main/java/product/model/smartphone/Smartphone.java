package product.model.smartphone;

import product.model.Product;
import product.model.ProductType;
import lombok.Getter;
import lombok.ToString;
import product.model.smartphone.configuration.SmartphoneConfiguration;

import java.math.BigDecimal;

@Getter
@ToString
public class Smartphone extends Product {
    private final SmartphoneConfiguration smartphoneConfiguration;

    public Smartphone(Long id, String name, BigDecimal basePrice, Integer quantity, SmartphoneConfiguration smartphoneConfiguration) {
        super(id, name, basePrice, quantity);
        this.smartphoneConfiguration = smartphoneConfiguration;
        productType = ProductType.SMARTPHONE;
    }

    @Override
    public BigDecimal getPrice() {
        return basePrice.add(smartphoneConfiguration.calculateAdditionalPrice());
    }
}
