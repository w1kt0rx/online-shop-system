package product.model.smartphone;

import java.math.BigDecimal;
import java.util.Set;
import lombok.Getter;
import lombok.ToString;
import product.model.Product;
import product.model.ProductType;
import product.model.smartphone.configuration.Accessory;
import product.model.smartphone.configuration.BatteryCapacity;
import product.model.smartphone.configuration.Color;
import product.model.smartphone.configuration.SmartphoneConfiguration;

@Getter
@ToString
public class Smartphone extends Product {

    private final SmartphoneConfiguration smartphoneConfiguration;

    public Smartphone(
        Long id,
        String name,
        BigDecimal basePrice,
        Integer quantity,
        SmartphoneConfiguration smartphoneConfiguration
    ) {
        super(id, name, basePrice, quantity, ProductType.SMARTPHONE);
        this.smartphoneConfiguration = smartphoneConfiguration;
    }

    public void configureSmartphone(Color color, BatteryCapacity batteryCapacity, Set<Accessory> accessories) {
        smartphoneConfiguration.configure(color, batteryCapacity, accessories);
    }

    @Override
    public BigDecimal getPrice() {
        return basePrice.add(smartphoneConfiguration.calculateAdditionalPrice());
    }
}
