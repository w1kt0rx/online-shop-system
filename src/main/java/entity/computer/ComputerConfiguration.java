package entity.computer;

import exception.InvalidGraphicsCardException;
import exception.InvalidProcessorException;
import exception.InvalidRamException;
import exception.InvalidStorageTypeException;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ComputerConfiguration {
    private Processor processor;
    private RAM ram;
    private StorageType storageType;
    private GraphicsCard graphicsCard;

    public ComputerConfiguration() {
        this.processor = Processor.INTEL_I5;
        this.ram = RAM.RAM_8GB;
        this.storageType = StorageType.SSD_512GB;
        this.graphicsCard = GraphicsCard.INTEGRATED;
    }

    public void configure(
            Processor processor,
            RAM ram,
            StorageType storage,
            GraphicsCard graphicsCard
    ) {

        validateProcessor(processor);
        validateRam(ram);
        validateStorageType(storage);
        validateGraphicsCard(graphicsCard);

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

    private void validateProcessor(Processor processor) {
        if (processor == null) {
            throw new InvalidProcessorException("Processor cannot be empty");
        }
    }

    private void validateRam(RAM ram) {
        if (ram == null) {
            throw new InvalidRamException("Ram cannot be empty");
        }
    }

    private void validateStorageType(StorageType storageType) {
        if (storageType == null) {
            throw new InvalidStorageTypeException("Storage type cannot be empty");
        }
    }

    private void validateGraphicsCard(GraphicsCard graphicsCard) {
        if (graphicsCard == null) {
            throw new InvalidGraphicsCardException("Graphics card cannot be empty");
        }
    }
}
