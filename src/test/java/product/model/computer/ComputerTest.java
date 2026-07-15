package product.model.computer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.model.computer.configuration.*;

public class ComputerTest {

    private Computer computer;

    @BeforeEach
    void setup() {
        computer = new Computer(1L, "Gaming PC", BigDecimal.valueOf(3000), 5, new ComputerConfiguration());
    }

    @Test
    void shouldConfigureComputerCorrectly() {
        computer.configureComputer(Processor.INTEL_I9, Ram.RAM_32GB, StorageType.SSD_2TB, GraphicsCard.RTX_4070);

        assertEquals(Processor.INTEL_I9, computer.getComputerConfiguration().getProcessor());

        assertEquals(Ram.RAM_32GB, computer.getComputerConfiguration().getRam());

        assertEquals(StorageType.SSD_2TB, computer.getComputerConfiguration().getStorageType());

        assertEquals(GraphicsCard.RTX_4070, computer.getComputerConfiguration().getGraphicsCard());
    }

    @Test
    void shouldCalculatePriceIncludingConfiguration() {
        BigDecimal basePrice = computer.getBasePrice();

        computer.configureComputer(Processor.INTEL_I9, Ram.RAM_32GB, StorageType.SSD_2TB, GraphicsCard.RTX_4070);

        assertTrue(computer.getPrice().compareTo(basePrice) > 0);
    }
}
