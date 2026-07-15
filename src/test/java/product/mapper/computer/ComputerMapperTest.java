package product.mapper.computer;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import product.dto.computer.ComputerDto;
import product.model.ProductType;
import product.model.computer.Computer;
import product.model.computer.configuration.*;

class ComputerMapperTest {

    @Test
    void shouldMapComputerToDtoWithAllFields() {
        Computer computer = new Computer(1L, "Dell XPS", new BigDecimal("3000"), 5, new ComputerConfiguration());

        ComputerDto dto = ComputerMapper.toDTO(computer);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Dell XPS");
        assertThat(dto.basePrice()).isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(dto.quantity()).isEqualTo(5);
        assertThat(dto.productType()).isEqualTo(ProductType.COMPUTER);
        assertThat(dto.computerConfiguration()).isNotNull();
    }

    @Test
    void shouldMapDtoBackToEntity() {
        Computer original = new Computer(1L, "Dell XPS", new BigDecimal("3000"), 5, new ComputerConfiguration());
        ComputerDto dto = ComputerMapper.toDTO(original);

        Computer entity = ComputerMapper.toEntity(dto);

        assertThat(entity.getId()).isEqualTo(original.getId());
        assertThat(entity.getName()).isEqualTo(original.getName());
        assertThat(entity.getBasePrice()).isEqualByComparingTo(original.getBasePrice());
        assertThat(entity.getQuantity()).isEqualTo(original.getQuantity());
    }

    @Test
    void shouldCarryConfiguredPriceInTotalPrice() {
        ComputerConfiguration cfg = new ComputerConfiguration();
        cfg.configure(Processor.INTEL_I9, Ram.RAM_32GB, StorageType.SSD_2TB, GraphicsCard.RTX_4070);
        Computer computer = new Computer(1L, "Gaming PC", new BigDecimal("2000"), 3, cfg);

        ComputerDto dto = ComputerMapper.toDTO(computer);

        // 2000 + 4900 = 6900
        assertThat(dto.totalPrice()).isEqualByComparingTo(new BigDecimal("6900"));
        assertThat(dto.basePrice()).isEqualByComparingTo(new BigDecimal("2000"));
    }

    @ParameterizedTest
    @MethodSource("provideComputers")
    void shouldMapIdCorrectly(Long id, String name, BigDecimal price) {
        Computer computer = new Computer(id, name, price, 1, new ComputerConfiguration());
        ComputerDto dto = ComputerMapper.toDTO(computer);

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo(name);
    }

    private static Stream<Arguments> provideComputers() {
        return Stream.of(
            Arguments.of(1L, "Dell XPS", new BigDecimal("3000")),
            Arguments.of(2L, "MacBook Pro", new BigDecimal("7000")),
            Arguments.of(3L, "Lenovo ThinkPad", new BigDecimal("2500"))
        );
    }
}
