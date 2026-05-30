package product.validator;

import product.model.computer.configuration.GraphicsCard;
import product.model.computer.configuration.Processor;
import product.model.computer.configuration.Ram;
import product.model.computer.configuration.StorageType;
import exception.InvalidConfigurationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComputerConfigurationValidator {

    public static void validate(Processor processor, Ram ram, StorageType storageType, GraphicsCard graphicsCard) {
        List<String> errors = new ArrayList<>();
        validateProcessor(processor, errors);
        validateRam(ram, errors);
        validateStorageType(storageType, errors);
        validateGraphicsCard(graphicsCard, errors);

        if(!errors.isEmpty()) {
            throw new InvalidConfigurationException(String.join(", ", errors));
        }
    }

    private static void validateProcessor(Processor processor, List<String> errors) {
        if (processor == null) {
            errors.add("Processor cannot be null");
        }
    }

    private static void validateRam(Ram ram, List<String> errors) {
        if (ram == null) {
            errors.add("Ram cannot be null");
        }
    }

    private static void validateStorageType(StorageType storageType, List<String> errors) {
        if (storageType == null) {
            errors.add("Storage type cannot be null");
        }
    }

    private static void validateGraphicsCard(GraphicsCard graphicsCard, List<String> errors) {
        if (graphicsCard == null) {
            errors.add("Graphics card cannot be null");
        }
    }

    public static void validateProcessor(Processor processor) {
        if (processor == null) {
            throw new InvalidConfigurationException("Processor cannot be null");
        }
    }

    public static void validateRam(Ram ram) {
        if (ram == null) {
            throw new InvalidConfigurationException("Ram cannot be null");
        }
    }

    public static void validateStorageType(StorageType storageType) {
        if (storageType == null) {
            throw new InvalidConfigurationException("Storage type cannot be null");
        }
    }

    public static void validateGraphicsCard(GraphicsCard graphicsCard) {
        if (graphicsCard == null) {
            throw new InvalidConfigurationException("Graphics card cannot be null");
        }
    }


}
