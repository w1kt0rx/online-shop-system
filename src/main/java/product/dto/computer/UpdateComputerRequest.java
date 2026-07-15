package product.dto.computer;

import java.math.BigDecimal;
import product.model.computer.configuration.GraphicsCard;
import product.model.computer.configuration.Processor;
import product.model.computer.configuration.Ram;
import product.model.computer.configuration.StorageType;

public record UpdateComputerRequest(
    String name,
    BigDecimal basePrice,
    Integer quantity,
    Processor processor,
    Ram ram,
    StorageType storageType,
    GraphicsCard graphicsCard
) {}
