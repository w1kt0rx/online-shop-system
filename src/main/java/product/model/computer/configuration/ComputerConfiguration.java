package product.model.computer.configuration;

import lombok.Getter;
import product.validator.ComputerConfigurationValidator;

import java.math.BigDecimal;

@Getter
public class ComputerConfiguration {
    private Processor processor;
    private Ram ram;
    private StorageType storageType;
    private GraphicsCard graphicsCard;

    public ComputerConfiguration() {
        this.processor = Processor.INTEL_I5;
        this.ram = Ram.RAM_8GB;
        this.storageType = StorageType.SSD_512GB;
        this.graphicsCard = GraphicsCard.INTEGRATED;
    }

    public void configure(Processor processor, Ram ram, StorageType storage, GraphicsCard graphicsCard) {
        ComputerConfigurationValidator.validate(processor, ram, storage, graphicsCard);

        this.processor = processor;
        this.ram = ram;
        this.storageType = storage;
        this.graphicsCard = graphicsCard;
    }

    public BigDecimal calculatePrice() {
        return processor.getPrice()
                .add(ram.getPrice())
                .add(storageType.getPrice())
                .add(graphicsCard.getPrice());
    }

    public void updateProcessor(Processor processor) {
        ComputerConfigurationValidator.validateProcessor(processor);
        this.processor = processor;
    }

    public void updateRam(Ram ram) {
        ComputerConfigurationValidator.validateRam(ram);
        this.ram = ram;
    }

    public void updateStorageType(StorageType storageType) {
        ComputerConfigurationValidator.validateStorageType(storageType);
        this.storageType = storageType;
    }

    public void updateGraphicsCard(GraphicsCard graphicsCard) {
        ComputerConfigurationValidator.validateGraphicsCard(graphicsCard);
        this.graphicsCard = graphicsCard;
    }
}
