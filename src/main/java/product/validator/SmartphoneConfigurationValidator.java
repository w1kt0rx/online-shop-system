package product.validator;

import product.model.smartphone.configuration.Accessory;
import product.model.smartphone.configuration.BatteryCapacity;
import product.model.smartphone.configuration.Color;
import exception.InvalidConfigurationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SmartphoneConfigurationValidator {

    public static void validateBattery(BatteryCapacity batteryCapacity) {
        if (batteryCapacity == null) {
            throw new InvalidConfigurationException("Battery capacity cannot be null");
        }
    }

    public static void validateAccessory(Accessory accessory) {
        if (accessory == null) {
            throw new InvalidConfigurationException("Accessory cannot be null");
        }
    }

    public static void validateColor(Color color) {
        if (color == null) {
            throw new InvalidConfigurationException("Color cannot be null");
        }
    }
}
