package entity;

import entity.computer.*;
import entity.smartphone.Smartphone;
import exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

public class ProductTest {

    private Computer computer;
    private Smartphone smartphone;
    private Electronics electronics;

    @BeforeEach
    public void setup() {
        computer = new Computer(1L, "Dell XPS", new BigDecimal("3000"), 20);
        smartphone = new Smartphone(2L, "iPhone", new BigDecimal("2500"), 15);
        electronics = new Electronics(3L, "Monitor", new BigDecimal("700"), 10);
    }

    @Test
    public void shouldThrowInvalidIdException() {
        assertThatExceptionOfType(InvalidIdException.class).isThrownBy(() -> new Computer(null, "Dell XPS", new BigDecimal("3000"), 20));
    }

    @Test
    public void shouldThrowInvalidNameException() {
        assertThatExceptionOfType(InvalidNameException.class).isThrownBy(() -> new Computer(1L, null, new BigDecimal("3000"), 20));
    }

    @Test
    public void shouldThrowInvalidPriceException() {
        assertThatExceptionOfType(InvalidPriceException.class).isThrownBy(() -> new Computer(1L, "Laptop", new BigDecimal("-3000"), 20));
    }

    @Test
    public void shouldThrowInvalidQuantityException() {
        assertThatExceptionOfType(InvalidQuantityException.class).isThrownBy(() -> new Computer(1L, "Laptop", new BigDecimal("3000"), -20));
    }

    @Test
    public void shouldReturnTrueWhenProductQuantityIsGreaterThanZero() {
        assertTrue(computer.isAvailable());
    }

    @Test
    public void shouldReturnFalseWhenProductQuantityIsZero() {
        computer.setQuantity(0);
        assertFalse(computer.isAvailable());
    }

    @Test
    public void shouldConfigureComputerCorrectly() {
        computer.configureComputer(
                Processor.INTEL_I9,
                RAM.RAM_32GB,
                StorageType.SSD_2TB,
                GraphicsCard.RTX_4070
        );
        assertEquals(Processor.INTEL_I9, computer.getComputerConfiguration().getProcessor());
        assertEquals(RAM.RAM_32GB, computer.getComputerConfiguration().getRam());
        assertEquals(StorageType.SSD_2TB, computer.getComputerConfiguration().getStorageType());
        assertEquals(GraphicsCard.RTX_4070, computer.getComputerConfiguration().getGraphicsCard());
    }

    @Test
    public void shouldThrowExceptionWhenProcessorIsNull() {
        assertThrows(
                InvalidProcessorException.class,
                () -> computer.configureComputer(
                        null,
                        RAM.RAM_32GB,
                        StorageType.SSD_2TB,
                        GraphicsCard.RTX_4070
                ));
    }
    @Test
    public void shouldThrowExceptionWhenRamIsNull() {
        assertThrows(
                InvalidRamException.class,
                () -> computer.configureComputer(
                        Processor.INTEL_I5,
                        null,
                        StorageType.SSD_2TB,
                        GraphicsCard.RTX_4070
                ));
    }
    @Test
    public void shouldThrowExceptionWhenStorageTypeIsNull() {
        assertThrows(
                InvalidStorageTypeException.class,
                () -> computer.configureComputer(
                        Processor.INTEL_I5,
                        RAM.RAM_8GB,
                        null,
                        GraphicsCard.RTX_4070
                ));
    }
    @Test
    public void shouldThrowExceptionWhenGraphicsCardIsNull() {
        assertThrows(
                InvalidGraphicsCardException.class,
                () -> computer.configureComputer(
                        Processor.INTEL_I5,
                        RAM.RAM_8GB,
                        StorageType.SSD_2TB,
                        null
                ));
    }
    @Test
    public void shouldCalculateCorrectPriceForDefaultConfiguration(){
        assertEquals(new BigDecimal("4000"), computer.getPrice());
    }
}
