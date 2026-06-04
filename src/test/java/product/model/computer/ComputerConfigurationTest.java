package product.model.computer;

import exception.InvalidConfigurationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import product.model.computer.configuration.*;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ComputerConfigurationTest {

    private ComputerConfiguration config;

    @BeforeEach
    void setUp() {
        config = new ComputerConfiguration();
    }

    @Test
    void shouldHaveCorrectDefaults() {
        assertThat(config.getProcessor()).isEqualTo(Processor.INTEL_I5);
        assertThat(config.getRam()).isEqualTo(Ram.RAM_8GB);
        assertThat(config.getStorageType()).isEqualTo(StorageType.SSD_512GB);
        assertThat(config.getGraphicsCard()).isEqualTo(GraphicsCard.INTEGRATED);
    }

    @Test
    void shouldConfigureAllComponents() {
        config.configure(Processor.INTEL_I9, Ram.RAM_64, StorageType.SSD_2TB, GraphicsCard.RTX_4070);

        assertThat(config.getProcessor()).isEqualTo(Processor.INTEL_I9);
        assertThat(config.getRam()).isEqualTo(Ram.RAM_64);
        assertThat(config.getStorageType()).isEqualTo(StorageType.SSD_2TB);
        assertThat(config.getGraphicsCard()).isEqualTo(GraphicsCard.RTX_4070);
    }

    @ParameterizedTest
    @MethodSource("provideAllProcessors")
    void shouldUpdateProcessorForAllValues(Processor processor) {
        config.updateProcessor(processor);
        assertThat(config.getProcessor()).isEqualTo(processor);
    }

    @ParameterizedTest
    @MethodSource("provideAllRams")
    void shouldUpdateRamForAllValues(Ram ram) {
        config.updateRam(ram);
        assertThat(config.getRam()).isEqualTo(ram);
    }

    @ParameterizedTest
    @MethodSource("provideAllStorageTypes")
    void shouldUpdateStorageTypeForAllValues(StorageType storage) {
        config.updateStorageType(storage);
        assertThat(config.getStorageType()).isEqualTo(storage);
    }

    @ParameterizedTest
    @MethodSource("provideAllGraphicsCards")
    void shouldUpdateGraphicsCardForAllValues(GraphicsCard gpu) {
        config.updateGraphicsCard(gpu);
        assertThat(config.getGraphicsCard()).isEqualTo(gpu);
    }

    @Test
    void shouldCalculateDefaultPrice() {
        // i5(500) + 8GB(200) + SSD512(300) + integrated(0) = 1000
        assertThat(config.calculatePrice()).isEqualByComparingTo(new BigDecimal("1000"));
    }

    @Test
    void shouldCalculateConfiguredPrice() {
        config.configure(Processor.INTEL_I9, Ram.RAM_32GB, StorageType.SSD_2TB, GraphicsCard.RTX_4070);
        // i9(1200) + 32GB(800) + SSD_2TB(900) + RTX4070(2000) = 4900
        assertThat(config.calculatePrice()).isEqualByComparingTo(new BigDecimal("4900"));
    }

    @Test
    void shouldThrowWhenConfiguringWithNullProcessor() {
        assertThatExceptionOfType(InvalidConfigurationException.class)
                .isThrownBy(() -> config.configure(null, Ram.RAM_8GB, StorageType.SSD_512GB, GraphicsCard.INTEGRATED));
    }

    @Test
    void shouldThrowWhenUpdatingProcessorToNull() {
        assertThatExceptionOfType(InvalidConfigurationException.class)
                .isThrownBy(() -> config.updateProcessor(null));
    }

    @Test
    void shouldThrowWhenUpdatingRamToNull() {
        assertThatExceptionOfType(InvalidConfigurationException.class)
                .isThrownBy(() -> config.updateRam(null));
    }

    @Test
    void shouldThrowWhenUpdatingStorageToNull() {
        assertThatExceptionOfType(InvalidConfigurationException.class)
                .isThrownBy(() -> config.updateStorageType(null));
    }

    @Test
    void shouldThrowWhenUpdatingGraphicsCardToNull() {
        assertThatExceptionOfType(InvalidConfigurationException.class)
                .isThrownBy(() -> config.updateGraphicsCard(null));
    }

    private static Stream<Arguments> provideAllProcessors() {
        return Stream.of(Processor.values()).map(Arguments::of);
    }

    private static Stream<Arguments> provideAllRams() {
        return Stream.of(Ram.values()).map(Arguments::of);
    }

    private static Stream<Arguments> provideAllStorageTypes() {
        return Stream.of(StorageType.values()).map(Arguments::of);
    }

    private static Stream<Arguments> provideAllGraphicsCards() {
        return Stream.of(GraphicsCard.values()).map(Arguments::of);
    }
}