package product.model.smartphone;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.model.smartphone.configuration.Accessory;
import product.model.smartphone.configuration.SmartphoneConfiguration;

public class SmartphoneTest {

    private Smartphone smartphone;

    @BeforeEach
    void setup() {
        smartphone = new Smartphone(1L, "Samsung", BigDecimal.valueOf(2000), 10, new SmartphoneConfiguration());
    }

    @Test
    void shouldAddAccessory() {
        smartphone.getSmartphoneConfiguration().addAccessory(Accessory.CHARGER);

        assertTrue(smartphone.getSmartphoneConfiguration().getAccessories().contains(Accessory.CHARGER));
    }

    @Test
    void shouldCalculateAdditionalPrice() {
        BigDecimal basePrice = smartphone.getPrice();

        smartphone.getSmartphoneConfiguration().addAccessory(Accessory.CHARGER);

        assertTrue(smartphone.getPrice().compareTo(basePrice) > 0);
    }
}
