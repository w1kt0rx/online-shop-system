package entity.computer;

import entity.Product;

import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;

@Getter
@ToString
public class Computer extends Product {
    private final ComputerConfiguration computerConfiguration;

    public Computer(Long id, String name, BigDecimal basePrice, int quantity) {
        super(id, name, basePrice, quantity);
        this.computerConfiguration = new ComputerConfiguration();

    }

    public void configureComputer(Processor processor, RAM ram, StorageType storage, GraphicsCard graphicsCard) {
        computerConfiguration.configure(processor, ram, storage, graphicsCard);
    }

    public BigDecimal getPrice() {
        return basePrice.add(computerConfiguration.calculatePrice());
    }
}
