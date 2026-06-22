package product.model.smartphone.configuration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Accessory {
    SCREEN_PROTECTOR("Screen protector", new BigDecimal("50")),
    PHONE_CASE("Phone case", new BigDecimal("80")),
    CHARGER("Charger", new BigDecimal("120")),
    HEADPHONES("Headphones", new BigDecimal("200")),
    CABLE("Cable", new BigDecimal("40"));

    private final String description;
    private final BigDecimal price;
}
