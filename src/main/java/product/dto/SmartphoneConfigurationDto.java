package product.dto;

import product.model.smartphone.configuration.Accessory;
import product.model.smartphone.configuration.BatteryCapacity;
import product.model.smartphone.configuration.Color;

import java.util.Set;

public record SmartphoneConfigurationDto(Color color, BatteryCapacity batteryCapacity, Set<Accessory> accessories) {
}
