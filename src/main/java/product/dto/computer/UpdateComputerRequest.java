package product.dto.computer;

import product.model.computer.configuration.GraphicsCard;
import product.model.computer.configuration.Processor;
import product.model.computer.configuration.Ram;
import product.model.computer.configuration.StorageType;

import java.math.BigDecimal;

public record UpdateComputerRequest(String name,
                                    BigDecimal basePrice,
                                    Integer quantity,
                                    Processor processor,
                                    Ram ram,
                                    StorageType storageType,
                                    GraphicsCard graphicsCard) {
}