package entity.smartphone;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum Accessory {
    SCREEN_PROTECTOR("Screen protector", new BigDecimal("50")),
    PHONE_CASE("Phone case", new BigDecimal("80")),
    CHARGER("Charger", new BigDecimal("120")),
    HEADPHONES("Headphones", new BigDecimal("200")),
    CABLE("Cable", new BigDecimal("40"));

    private final String description;
    private final BigDecimal price;

    Accessory(String description, BigDecimal price) {
        this.description = description;
        this.price = price;
    }
}
