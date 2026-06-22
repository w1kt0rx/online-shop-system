package product.validator;

import exception.InvalidConfigurationException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import product.model.computer.configuration.GraphicsCard;
import product.model.computer.configuration.Processor;
import product.model.computer.configuration.Ram;
import product.model.computer.configuration.StorageType;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ComputerConfigurationValidator {

    public static void validate(
            Processor processor,
            Ram ram,
            StorageType storageType,
            GraphicsCard graphicsCard
    ) {
        List<String> errors = new ArrayList<>();

        validateRequired(processor, "Processor", errors);
        validateRequired(ram, "Ram", errors);
        validateRequired(storageType, "Storage type", errors);
        validateRequired(graphicsCard, "Graphics card", errors);

        if (!errors.isEmpty()) {
            throw new InvalidConfigurationException(String.join(", ", errors));
        }
    }

    private static void validateRequired(Object value, String fieldName, List<String> errors) {
        if (value == null) {
            errors.add(fieldName + " cannot be null");
        }
    }
}