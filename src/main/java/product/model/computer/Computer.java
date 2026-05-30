package product.model.computer;

import lombok.Getter;
import lombok.ToString;
import product.model.*;
import product.model.computer.configuration.*;

import java.math.BigDecimal;

@Getter
@ToString
public class Computer extends Product {
    private final ComputerConfiguration computerConfiguration;

    public Computer(Long id, String name, BigDecimal basePrice, Integer quantity, ComputerConfiguration computerConfiguration) {
        super(id, name, basePrice, quantity, ProductType.COMPUTER);
        this.computerConfiguration = computerConfiguration;

    }

    public void configureComputer(Processor processor, Ram ram, StorageType storage, GraphicsCard graphicsCard) {
        computerConfiguration.configure(processor, ram, storage, graphicsCard);
    }

    @Override
    public BigDecimal getPrice() {
        return basePrice.add(computerConfiguration.calculatePrice());
    }
}
