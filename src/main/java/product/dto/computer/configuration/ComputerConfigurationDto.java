package product.dto.computer.configuration;

import product.model.computer.configuration.GraphicsCard;
import product.model.computer.configuration.Processor;
import product.model.computer.configuration.Ram;
import product.model.computer.configuration.StorageType;

public record ComputerConfigurationDto(
    Processor processor,
    Ram ram,
    StorageType storageType,
    GraphicsCard graphicsCard
) {}
