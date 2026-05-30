package product.validator;

import exception.InvalidConfigurationException;
import org.junit.jupiter.api.Test;
import product.model.smartphone.configuration.Accessory;
import product.model.smartphone.configuration.BatteryCapacity;
import product.model.smartphone.configuration.Color;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SmartphoneConfigurationValidatorTest {

    @Test
    void shouldPassValidation() {
        assertDoesNotThrow(() ->
                SmartphoneConfigurationValidator.validate(
                        Color.BLACK,
                        BatteryCapacity.BATTERY_4000,
                        Set.of(Accessory.CHARGER)
                )
        );
    }

    @Test
    void shouldPassValidationWithEmptyAccessories() {
        assertDoesNotThrow(() ->
                SmartphoneConfigurationValidator.validate(
                        Color.GOLD,
                        BatteryCapacity.BATTERY_6000,
                        Set.of()
                )
        );
    }

    @Test
    void shouldThrowWhenColorIsNull() {
        assertThrows(
                InvalidConfigurationException.class,
                () -> SmartphoneConfigurationValidator.validate(
                        null,
                        BatteryCapacity.BATTERY_4000,
                        Set.of()
                )
        );
    }

    @Test
    void shouldThrowWhenBatteryCapacityIsNull() {
        assertThrows(
                InvalidConfigurationException.class,
                () -> SmartphoneConfigurationValidator.validate(
                        Color.BLACK,
                        null,
                        Set.of()
                )
        );
    }

    @Test
    void shouldThrowWhenAccessoriesIsNull() {
        assertThrows(
                InvalidConfigurationException.class,
                () -> SmartphoneConfigurationValidator.validate(
                        Color.BLACK,
                        BatteryCapacity.BATTERY_4000,
                        null
                )
        );
    }

    @Test
    void shouldCollectMultipleValidationErrors() {
        InvalidConfigurationException exception = assertThrows(
                InvalidConfigurationException.class,
                () -> SmartphoneConfigurationValidator.validate(null, null, null)
        );

        assertTrue(exception.getMessage().contains("Color cannot be null"));
        assertTrue(exception.getMessage().contains("Battery capacity cannot be null"));
        assertTrue(exception.getMessage().contains("Accessory cannot be null"));
    }

    @Test
    void shouldThrowWhenSingleColorIsNull() {
        assertThrows(
                InvalidConfigurationException.class,
                () -> SmartphoneConfigurationValidator.validateColor(null)
        );
    }

    @Test
    void shouldThrowWhenSingleBatteryIsNull() {
        assertThrows(
                InvalidConfigurationException.class,
                () -> SmartphoneConfigurationValidator.validateBattery(null)
        );
    }

    @Test
    void shouldThrowWhenSingleAccessoryIsNull() {
        assertThrows(
                InvalidConfigurationException.class,
                () -> SmartphoneConfigurationValidator.validateAccessory((Accessory) null)
        );
    }
}
