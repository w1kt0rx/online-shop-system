package product.mapper;


import org.junit.jupiter.api.Test;
import product.dto.SmartphoneDto;
import product.model.ProductType;
import product.model.smartphone.Smartphone;
import product.model.smartphone.configuration.*;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class SmartphoneMapperTest {

    @Test
    void shouldMapSmartphoneToDtoWithAllFields() {
        Smartphone smartphone = new Smartphone(1L, "iPhone 15", new BigDecimal("4000"), 10, new SmartphoneConfiguration());

        SmartphoneDto dto = SmartphoneMapper.toDTO(smartphone);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("iPhone 15");
        assertThat(dto.basePrice()).isEqualByComparingTo(new BigDecimal("4000"));
        assertThat(dto.quantity()).isEqualTo(10);
        assertThat(dto.productType()).isEqualTo(ProductType.SMARTPHONE);
        assertThat(dto.smartphoneConfiguration()).isNotNull();
    }

    @Test
    void shouldMapDtoBackToEntity() {
        Smartphone original = new Smartphone(2L, "Samsung S24", new BigDecimal("3500"), 8, new SmartphoneConfiguration());
        SmartphoneDto dto = SmartphoneMapper.toDTO(original);

        Smartphone entity = SmartphoneMapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(original.getId());
        assertThat(entity.getName()).isEqualTo(original.getName());
        assertThat(entity.getBasePrice()).isEqualByComparingTo(original.getBasePrice());
    }

    @Test
    void shouldCarryConfiguredPriceInTotalPrice() {
        SmartphoneConfiguration cfg = new SmartphoneConfiguration();
        cfg.configure(Color.GOLD, BatteryCapacity.BATTERY_6000, Set.of(Accessory.CHARGER));
        Smartphone smartphone = new Smartphone(1L, "iPhone", new BigDecimal("4000"), 5, cfg);

        SmartphoneDto dto = SmartphoneMapper.toDTO(smartphone);

        // 4000 + GOLD(100) + BATTERY_6000(500) + CHARGER(120) = 4720
        assertThat(dto.totalPrice()).isEqualByComparingTo(new BigDecimal("4720"));
        assertThat(dto.basePrice()).isEqualByComparingTo(new BigDecimal("4000"));
    }
}