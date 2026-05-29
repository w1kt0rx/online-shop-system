package product.model.smartphone.configuration;

import product.model.computer.configuration.GraphicsCard;
import product.model.computer.configuration.Processor;
import product.model.computer.configuration.Ram;
import product.model.computer.configuration.StorageType;
import product.validator.ComputerConfigurationValidator;
import product.validator.SmartphoneConfigurationValidator;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class SmartphoneConfiguration {
    private Color color;
    private BatteryCapacity batteryCapacity;
    private Set<Accessory> accessories;

    public SmartphoneConfiguration() {
        this.color = Color.BLACK;
        this.batteryCapacity = BatteryCapacity.BATTERY_4000;
        accessories = new HashSet<>();
    }

    public void configure(Color color, BatteryCapacity batteryCapacity, Set<Accessory> accessories) {
        this.color = color;
        this.batteryCapacity = batteryCapacity;
        this.accessories = accessories;
    }

    public void selectColor(Color color) {
        SmartphoneConfigurationValidator.validateColor(color);

        this.color = color;
    }

    public void selectBatteryCapacity(BatteryCapacity batteryCapacity) {
        SmartphoneConfigurationValidator.validateBattery(batteryCapacity);

        this.batteryCapacity = batteryCapacity;
    }

    public void addAccessory(Accessory accessory) {
        SmartphoneConfigurationValidator.validateAccessory(accessory);

        accessories.add(accessory);
    }

    public void removeAccessory(Accessory accessory) {
        accessories.remove(accessory);
    }

    public void clearAccessories() {
        accessories.clear();
    }

    public BigDecimal calculateAdditionalPrice() {
        BigDecimal total = color.getPrice()
                .add(batteryCapacity.getPrice());

        for (Accessory accessory : accessories) {
            total = total.add(accessory.getPrice());
        }

        return total;
    }
}