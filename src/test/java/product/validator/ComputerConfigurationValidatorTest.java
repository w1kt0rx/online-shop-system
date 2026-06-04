package product.validator;


import exception.InvalidConfigurationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import product.model.computer.configuration.*;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ComputerConfigurationValidatorTest {

    @Test
    void shouldPassValidation() {
        assertThatCode(() -> ComputerConfigurationValidator.validate(
                Processor.INTEL_I7, Ram.RAM_16GB, StorageType.SSD_1TB, GraphicsCard.RTX_3050))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("provideAllValidCombinations")
    void shouldPassValidationForAllEnumCombinations(
            Processor p, Ram r, StorageType s, GraphicsCard g) {
        assertThatCode(() -> ComputerConfigurationValidator.validate(p, r, s, g))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowWhenProcessorIsNull() {
        assertThatExceptionOfType(InvalidConfigurationException.class)
                .isThrownBy(() -> ComputerConfigurationValidator.validate(
                        null, Ram.RAM_8GB, StorageType.SSD_512GB, GraphicsCard.INTEGRATED))
                .withMessageContaining("Processor cannot be null");
    }

    @Test
    void shouldThrowWhenRamIsNull() {
        assertThatExceptionOfType(InvalidConfigurationException.class)
                .isThrownBy(() -> ComputerConfigurationValidator.validate(
                        Processor.INTEL_I5, null, StorageType.SSD_512GB, GraphicsCard.INTEGRATED))
                .withMessageContaining("Ram cannot be null");
    }

    @Test
    void shouldThrowWhenStorageIsNull() {
        assertThatExceptionOfType(InvalidConfigurationException.class)
                .isThrownBy(() -> ComputerConfigurationValidator.validate(
                        Processor.INTEL_I5, Ram.RAM_8GB, null, GraphicsCard.INTEGRATED))
                .withMessageContaining("Storage type cannot be null");
    }

    @Test
    void shouldThrowWhenGraphicsCardIsNull() {
        assertThatExceptionOfType(InvalidConfigurationException.class)
                .isThrownBy(() -> ComputerConfigurationValidator.validate(
                        Processor.INTEL_I5, Ram.RAM_8GB, StorageType.SSD_512GB, null))
                .withMessageContaining("Graphics card cannot be null");
    }

    @Test
    void shouldCollectAllErrorsWhenAllNull() {
        assertThatExceptionOfType(InvalidConfigurationException.class)
                .isThrownBy(() -> ComputerConfigurationValidator.validate(null, null, null, null))
                .withMessageContaining("Processor cannot be null")
                .withMessageContaining("Ram cannot be null")
                .withMessageContaining("Storage type cannot be null")
                .withMessageContaining("Graphics card cannot be null");
    }

    @Test
    void shouldThrowWhenValidateProcessorCalledWithNull() {
        assertThatExceptionOfType(InvalidConfigurationException.class)
                .isThrownBy(() -> ComputerConfigurationValidator.validateProcessor(null));
    }

    @Test
    void shouldThrowWhenValidateRamCalledWithNull() {
        assertThatExceptionOfType(InvalidConfigurationException.class)
                .isThrownBy(() -> ComputerConfigurationValidator.validateRam(null));
    }

    @Test
    void shouldThrowWhenValidateStorageCalledWithNull() {
        assertThatExceptionOfType(InvalidConfigurationException.class)
                .isThrownBy(() -> ComputerConfigurationValidator.validateStorageType(null));
    }

    @Test
    void shouldThrowWhenValidateGraphicsCardCalledWithNull() {
        assertThatExceptionOfType(InvalidConfigurationException.class)
                .isThrownBy(() -> ComputerConfigurationValidator.validateGraphicsCard(null));
    }

    private static Stream<Arguments> provideAllValidCombinations() {
        return Stream.of(
                Arguments.of(Processor.INTEL_I5, Ram.RAM_8GB, StorageType.SSD_512GB, GraphicsCard.INTEGRATED),
                Arguments.of(Processor.INTEL_I7, Ram.RAM_16GB, StorageType.SSD_1TB, GraphicsCard.RTX_3050),
                Arguments.of(Processor.INTEL_I9, Ram.RAM_32GB, StorageType.SSD_2TB, GraphicsCard.RTX_4070),
                Arguments.of(Processor.AMD_RYZEN_5, Ram.RAM_8GB, StorageType.HDD_1TB, GraphicsCard.GTX_1650),
                Arguments.of(Processor.AMD_RYZEN_7, Ram.RAM_64, StorageType.HDD_2TB, GraphicsCard.RTX_4070)
        );
    }
}