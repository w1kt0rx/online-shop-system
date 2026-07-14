package product.dto.smartphone.configuration;

import java.util.Set;
import product.model.smartphone.configuration.Accessory;
import product.model.smartphone.configuration.BatteryCapacity;
import product.model.smartphone.configuration.Color;

public record SmartphoneConfigurationDto(Color color, BatteryCapacity batteryCapacity, Set<Accessory> accessories) {}
