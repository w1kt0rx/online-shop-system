package product.model;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import exception.InvalidConfigurationException;
import exception.InvalidProductException;
import exception.NotEnoughStockException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.model.computer.*;
import product.model.computer.configuration.*;
import product.model.electronics.Electronics;
import product.model.smartphone.Smartphone;
import product.model.smartphone.configuration.SmartphoneConfiguration;

public class ProductTest {

    private Computer computer;
    private Smartphone smartphone;
    private Electronics electronics;

    @BeforeEach
    public void setup() {
        computer = new Computer(1L, "Dell XPS", new BigDecimal("3000"), 20, new ComputerConfiguration());
        smartphone = new Smartphone(2L, "iPhone", new BigDecimal("2500"), 15, new SmartphoneConfiguration());
        electronics = new Electronics(3L, "Monitor", new BigDecimal("700"), 10);
    }

    @Test
    public void shouldThrowInvalidNameException() {
        assertThatExceptionOfType(InvalidProductException.class).isThrownBy(() ->
            new Computer(1L, null, new BigDecimal("3000"), 20, new ComputerConfiguration())
        );
    }

    @Test
    public void shouldThrowInvalidPriceException() {
        assertThatExceptionOfType(InvalidProductException.class).isThrownBy(() ->
            new Computer(1L, "Laptop", new BigDecimal("-3000"), 20, new ComputerConfiguration())
        );
    }

    @Test
    public void shouldThrowInvalidQuantityException() {
        assertThatExceptionOfType(InvalidProductException.class).isThrownBy(() ->
            new Computer(1L, "Laptop", new BigDecimal("3000"), -20, new ComputerConfiguration())
        );
    }

    @Test
    public void shouldReturnTrueWhenProductQuantityIsGreaterThanZero() {
        assertTrue(computer.isAvailable());
    }

    @Test
    public void shouldReturnFalseWhenProductQuantityIsZero() {
        Electronics emptyStock = new Electronics(4L, "Cable", new BigDecimal("50"), 0);
        assertFalse(emptyStock.isAvailable());
    }

    @Test
    public void shouldConfigureComputerCorrectly() {
        computer.configureComputer(Processor.INTEL_I9, Ram.RAM_32GB, StorageType.SSD_2TB, GraphicsCard.RTX_4070);
        assertEquals(Processor.INTEL_I9, computer.getComputerConfiguration().getProcessor());
        assertEquals(Ram.RAM_32GB, computer.getComputerConfiguration().getRam());
        assertEquals(StorageType.SSD_2TB, computer.getComputerConfiguration().getStorageType());
        assertEquals(GraphicsCard.RTX_4070, computer.getComputerConfiguration().getGraphicsCard());
    }

    @Test
    public void shouldThrowExceptionWhenProcessorIsNull() {
        assertThrows(InvalidConfigurationException.class, () ->
            computer.configureComputer(null, Ram.RAM_32GB, StorageType.SSD_2TB, GraphicsCard.RTX_4070)
        );
    }

    @Test
    public void shouldThrowExceptionWhenRamIsNull() {
        assertThrows(InvalidConfigurationException.class, () ->
            computer.configureComputer(Processor.INTEL_I5, null, StorageType.SSD_2TB, GraphicsCard.RTX_4070)
        );
    }

    @Test
    public void shouldThrowExceptionWhenStorageTypeIsNull() {
        assertThrows(InvalidConfigurationException.class, () ->
            computer.configureComputer(Processor.INTEL_I5, Ram.RAM_8GB, null, GraphicsCard.RTX_4070)
        );
    }

    @Test
    public void shouldThrowExceptionWhenGraphicsCardIsNull() {
        assertThrows(InvalidConfigurationException.class, () ->
            computer.configureComputer(Processor.INTEL_I5, Ram.RAM_8GB, StorageType.SSD_2TB, null)
        );
    }

    @Test
    public void shouldCalculateCorrectPriceForDefaultConfiguration() {
        assertEquals(new BigDecimal("4000"), computer.getPrice());
    }

    @Test
    public void shouldDecreaseQuantityByExactStock() {
        computer.decreaseQuantity(20);
        assertEquals(0, computer.getQuantity());
        assertFalse(computer.isAvailable());
    }

    @Test
    public void shouldThrowExceptionWhenDecreaseAmountExceedsStock() {
        assertThrows(NotEnoughStockException.class, () -> computer.decreaseQuantity(21));
    }

    @Test
    public void shouldThrowExceptionWhenDecreaseAmountIsNegative() {
        assertThrows(IllegalArgumentException.class, () -> computer.decreaseQuantity(-1));
    }

    @Test
    public void shouldIncreaseQuantityCorrectly() {
        computer.increaseQuantity(5);
        assertEquals(25, computer.getQuantity());
    }

    @Test
    public void shouldThrowExceptionWhenIncreaseAmountIsZeroOrNegative() {
        assertThrows(IllegalArgumentException.class, () -> computer.increaseQuantity(0));
        assertThrows(IllegalArgumentException.class, () -> computer.increaseQuantity(-1));
    }
}
