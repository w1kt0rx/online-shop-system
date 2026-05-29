package product.model.smartphone.configuration;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum BatteryCapacity {
    BATTERY_3000("3000 mAh", BigDecimal.ZERO),
    BATTERY_4000("4000 mAh", new BigDecimal("150")),
    BATTERY_5000("5000 mAh", new BigDecimal("300")),
    BATTERY_6000("6000 mAh", new BigDecimal("500"));

    private final String description;
    private final BigDecimal price;

    BatteryCapacity(String description, BigDecimal price) {
        this.description = description;
        this.price = price;
    }
}
