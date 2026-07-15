package product.model.electronics;

import static org.junit.jupiter.api.Assertions.*;

import exception.InvalidProductException;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ElectronicsTest {

    private Electronics electronics;

    @BeforeEach
    void setup() {
        electronics = new Electronics(1L, "Mouse", BigDecimal.valueOf(100), 10);
    }

    @Test
    void shouldCreateProductWhenDataIsValid() {
        assertEquals(1L, electronics.getId());
        assertEquals("Mouse", electronics.getName());
        assertEquals(BigDecimal.valueOf(100), electronics.getPrice());
        assertEquals(10, electronics.getQuantity());
    }

    @Test
    void shouldThrowExceptionWhenProductDataIsInvalid() {
        assertThrows(InvalidProductException.class, () -> new Electronics(null, "", BigDecimal.valueOf(-100), -10));
    }

    @Test
    void shouldIncreaseQuantity() {
        electronics.increaseQuantity(5);

        assertEquals(15, electronics.getQuantity());
    }

    @Test
    void shouldDecreaseQuantity() {
        electronics.decreaseQuantity(5);

        assertEquals(5, electronics.getQuantity());
    }

    @Test
    void shouldReturnAvailabilityStatus() {
        assertTrue(electronics.isAvailable());

        electronics.decreaseQuantity(10);

        assertFalse(electronics.isAvailable());
    }
}
