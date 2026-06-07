package product.validator;

import product.model.smartphone.configuration.Accessory;
import product.model.smartphone.configuration.BatteryCapacity;
import product.model.smartphone.configuration.Color;
import exception.InvalidConfigurationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SmartphoneConfigurationValidator {
    public static void validate(Color color, BatteryCapacity batteryCapacity, Set<Accessory> accessories) {
        List<String> errors = new ArrayList<>();
        validateColor(color, errors);
        validateBattery(batteryCapacity, errors);
        validateAccessory(accessories, errors);

        if (!errors.isEmpty()) {
            throw new InvalidConfigurationException(String.join(", ", errors));
        }

    }

    private static void validateBattery(BatteryCapacity batteryCapacity, List<String> errors) {
        if (batteryCapacity == null) {
            errors.add("Battery capacity cannot be null");
        }
    }

    private static void validateAccessory(Set<Accessory> accessories, List<String> errors) {
        if (accessories == null) {
            errors.add("Accessory cannot be null");
        }
    }

    private static void validateColor(Color color, List<String> errors) {
        if (color == null) {
            errors.add("Color cannot be null");
        }
    }


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
