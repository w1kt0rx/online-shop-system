package product.model.computer.configuration;

import java.math.BigDecimal;
import lombok.Getter;
import product.validator.ComputerConfigurationValidator;

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

    public void configure(Processor processor, Ram ram, StorageType storageType, GraphicsCard graphicsCard) {
        ComputerConfigurationValidator.validate(processor, ram, storageType, graphicsCard);

        this.processor = processor;
        this.ram = ram;
        this.storageType = storageType;
        this.graphicsCard = graphicsCard;
    }

    public BigDecimal calculatePrice() {
        return processor.getPrice().add(ram.getPrice()).add(storageType.getPrice()).add(graphicsCard.getPrice());
    }

    public void updateProcessor(Processor processor) {
        ComputerConfigurationValidator.validate(processor, this.ram, this.storageType, this.graphicsCard);
        this.processor = processor;
    }

    public void updateRam(Ram ram) {
        ComputerConfigurationValidator.validate(this.processor, ram, this.storageType, this.graphicsCard);
        this.ram = ram;
    }

    public void updateStorageType(StorageType storageType) {
        ComputerConfigurationValidator.validate(this.processor, this.ram, storageType, this.graphicsCard);
        this.storageType = storageType;
    }

    public void updateGraphicsCard(GraphicsCard graphicsCard) {
        ComputerConfigurationValidator.validate(this.processor, this.ram, this.storageType, graphicsCard);
        this.graphicsCard = graphicsCard;
    }
}
