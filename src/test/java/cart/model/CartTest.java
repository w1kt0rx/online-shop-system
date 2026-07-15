package cart.model;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.model.electronics.Electronics;

class CartTest {

    private Cart cart;
    private Electronics monitor;
    private Electronics keyboard;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        monitor = new Electronics(1L, "Monitor 4K", new BigDecimal("800"), 10);
        keyboard = new Electronics(2L, "Keyboard", new BigDecimal("150"), 20);
    }

    @Test
    void shouldAddProductToCart() {
        cart.addProduct(monitor, 2);

        assertEquals(1, cart.getCartItems().size());
        assertEquals(2, cart.getCartItems().get(0).getQuantity());
    }

    @Test
    void shouldAccumulateQuantityForSameProduct() {
        cart.addProduct(monitor, 2);
        cart.addProduct(monitor, 3);

        assertEquals(1, cart.getCartItems().size());
        assertEquals(5, cart.getCartItems().get(0).getQuantity());
    }

    @Test
    void shouldAddMultipleDifferentProducts() {
        cart.addProduct(monitor, 1);
        cart.addProduct(keyboard, 2);

        assertEquals(2, cart.getCartItems().size());
    }

    @Test
    void shouldRemoveProductFromCart() {
        cart.addProduct(monitor, 2);
        cart.addProduct(keyboard, 1);
        cart.removeProduct(monitor);

        assertEquals(1, cart.getCartItems().size());
        assertEquals("Keyboard", cart.getCartItems().get(0).getProduct().getName());
    }

    @Test
    void shouldNotThrowWhenRemovingNonExistentProduct() {
        assertDoesNotThrow(() -> cart.removeProduct(monitor));
        assertTrue(cart.getCartItems().isEmpty());
    }

    @Test
    void shouldCalculateTotalPriceCorrectly() {
        cart.addProduct(monitor, 2); // 2 × 800 = 1600
        cart.addProduct(keyboard, 3); // 3 × 150 = 450

        assertEquals(new BigDecimal("2050"), cart.getTotalPrice());
    }

    @Test
    void shouldReturnZeroForEmptyCart() {
        assertEquals(BigDecimal.ZERO, cart.getTotalPrice());
    }

    @Test
    void shouldClearCart() {
        cart.addProduct(monitor, 1);
        cart.addProduct(keyboard, 2);
        cart.clear();

        assertTrue(cart.getCartItems().isEmpty());
        assertTrue(cart.isEmpty());
    }

    @Test
    void shouldReturnTrueForEmptyCart() {
        assertTrue(cart.isEmpty());
    }

    @Test
    void shouldReturnFalseWhenCartHasItems() {
        cart.addProduct(monitor, 1);
        assertFalse(cart.isEmpty());
    }

    @Test
    void shouldThrowWhenAddingZeroQuantity() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> cart.addProduct(monitor, 0))
            .extracting(Throwable::getMessage)
            .isEqualTo("Quantity must be bigger than zero");
    }

    @Test
    void shouldThrowWhenAddingNegativeQuantity() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> cart.addProduct(monitor, -1))
            .extracting(Throwable::getMessage)
            .isEqualTo("Quantity must be bigger than zero");
    }

    @Test
    void shouldReturnImmutableListOfProducts() {
        cart.addProduct(monitor, 1);

        assertThrows(UnsupportedOperationException.class, () -> cart.getCartItems().add(new CartItem(keyboard, 1)));
    }
}
