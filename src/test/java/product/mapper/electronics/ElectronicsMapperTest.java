package product.mapper.electronics;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import product.dto.electronics.ElectronicsDto;
import product.model.ProductType;
import product.model.electronics.Electronics;

class ElectronicsMapperTest {

    @Test
    void shouldMapElectronicsToDtoWithAllFields() {
        Electronics electronics = new Electronics(1L, "Monitor 4K", new BigDecimal("1500"), 12);

        ElectronicsDto dto = ElectronicsMapper.toDTO(electronics);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Monitor 4K");
        assertThat(dto.basePrice()).isEqualByComparingTo(new BigDecimal("1500"));
        assertThat(dto.quantity()).isEqualTo(12);
        assertThat(dto.productType()).isEqualTo(ProductType.ELECTRONICS);
    }

    @Test
    void shouldMapDtoBackToEntity() {
        Electronics original = new Electronics(3L, "Keyboard", new BigDecimal("300"), 20);
        ElectronicsDto dto = ElectronicsMapper.toDTO(original);

        Electronics entity = ElectronicsMapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(original.getId());
        assertThat(entity.getName()).isEqualTo(original.getName());
        assertThat(entity.getBasePrice()).isEqualByComparingTo(original.getBasePrice());
        assertThat(entity.getQuantity()).isEqualTo(original.getQuantity());
    }

    @ParameterizedTest
    @MethodSource("provideElectronics")
    void shouldMapVariousElectronicsCorrectly(Long id, String name, BigDecimal price, int qty) {
        Electronics electronics = new Electronics(id, name, price, qty);
        ElectronicsDto dto = ElectronicsMapper.toDTO(electronics);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo(name);
        assertThat(dto.basePrice()).isEqualByComparingTo(price);
        assertThat(dto.quantity()).isEqualTo(qty);
    }

    private static Stream<Arguments> provideElectronics() {
        return Stream.of(
            Arguments.of(1L, "Headphones", new BigDecimal("200"), 50),
            Arguments.of(2L, "Webcam", new BigDecimal("350"), 30),
            Arguments.of(3L, "USB Hub", new BigDecimal("80"), 100)
        );
    }
}
