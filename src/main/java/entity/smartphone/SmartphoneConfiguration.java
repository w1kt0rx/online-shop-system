package entity.smartphone;

import exception.InvalidAccessoryException;
import exception.InvalidBatteryCapacityException;
import exception.InvalidColorException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class SmartphoneConfiguration {

    private Color color = Color.BLACK;

    private BatteryCapacity batteryCapacity =
            BatteryCapacity.BATTERY_4000;

    private final Set<Accessory> accessories =
            new HashSet<>();

    public void selectColor(Color color) {
        validateColor(color);

        this.color = color;
    }

    public void selectBatteryCapacity(
            BatteryCapacity batteryCapacity
    ) {
        validateBattery(batteryCapacity);

        this.batteryCapacity = batteryCapacity;
    }

    public void addAccessory(Accessory accessory) {
        validateAccessory(accessory);

        accessories.add(accessory);
    }

    public void removeAccessory(Accessory accessory) {
        accessories.remove(accessory);
    }

    public void clearAccessories() {
        accessories.clear();
    }

    public BigDecimal calculateAdditionalPrice() {

        BigDecimal total = BigDecimal.ZERO;

        total = total.add(color.getPrice());
        total = total.add(batteryCapacity.getPrice());

        for (Accessory accessory : accessories) {
            total = total.add(accessory.getPrice());
        }

        return total;
    }

    private void validateColor(Color color) {
        if (color == null) {
            throw new InvalidColorException("Color cannot be empty");
        }
    }

    private void validateBattery(
            BatteryCapacity batteryCapacity
    ) {
        if (batteryCapacity == null) {
            throw new InvalidBatteryCapacityException("Battery capacity cannot be empty");
        }
    }

    private void validateAccessory(
            Accessory accessory
    ) {
        if (accessory == null) {
            throw new InvalidAccessoryException("Accessory cannot be empty");
        }
    }
}