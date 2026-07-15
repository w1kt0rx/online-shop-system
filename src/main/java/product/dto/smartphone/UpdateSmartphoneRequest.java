package product.dto.smartphone;

import java.math.BigDecimal;
import java.util.Set;
import product.model.smartphone.configuration.Accessory;
import product.model.smartphone.configuration.BatteryCapacity;
import product.model.smartphone.configuration.Color;

public record UpdateSmartphoneRequest(
    String name,
    BigDecimal basePrice,
    Integer quantity,
    Set<Accessory> accessory,
    BatteryCapacity batteryCapacity,
    Color color
) {}
