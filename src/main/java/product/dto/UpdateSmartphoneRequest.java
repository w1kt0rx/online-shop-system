package product.dto;

import product.model.smartphone.configuration.Accessory;
import product.model.smartphone.configuration.BatteryCapacity;
import product.model.smartphone.configuration.Color;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record UpdateSmartphoneRequest(String name,
                                      BigDecimal basePrice,
                                      Integer quantity,
                                      Set<Accessory> accessory,
                                      BatteryCapacity batteryCapacity,
                                      Color color) {
}