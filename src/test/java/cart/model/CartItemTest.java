package cart.model;

import exception.InvalidProductException;
import exception.NotEnoughStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import product.model.electronics.Electronics;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

class CartItemTest {

    private Electronics product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        product = new Electronics(1L, "Monitor", new BigDecimal("800"), 10);
        cartItem = new CartItem(product, 2);
    }

    @Test
    void shouldCreateCartItemWithCorrectData() {
        assertThat(cartItem.getProduct()).isEqualTo(product);
        assertThat(cartItem.getQuantity()).isEqualTo(2);
    }

    @Test
    void shouldCalculateTotalPriceCorrectly() {
        // 800 * 2 = 1600
        assertThat(cartItem.calculateTotalPrice()).isEqualByComparingTo(new BigDecimal("1600"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    void shouldIncreaseQuantityByVariousAmounts(int amount) {
        int initial = cartItem.getQuantity();
        cartItem.increaseQuantity(amount);
        assertThat(cartItem.getQuantity()).isEqualTo(initial + amount);
    }

    @Test
    void shouldDecreaseQuantityCorrectly() {
        cartItem.decreaseQuantity(1);
        assertThat(cartItem.getQuantity()).isEqualTo(1);
    }

    @Test
    void shouldDecreaseQuantityToZero() {
        cartItem.decreaseQuantity(2);
        assertThat(cartItem.getQuantity()).isEqualTo(0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void shouldThrowWhenIncreaseAmountIsZeroOrNegative(int amount) {
        assertThatExceptionOfType(NotEnoughStockException.class)
                .isThrownBy(() -> cartItem.increaseQuantity(amount));
    }

    @Test
    void shouldThrowWhenDecreaseExceedsQuantity() {
        assertThatExceptionOfType(NotEnoughStockException.class)
                .isThrownBy(() -> cartItem.decreaseQuantity(3));
    }

    @Test
    void shouldThrowWhenDecreaseAmountIsNegative() {
        assertThatExceptionOfType(NotEnoughStockException.class)
                .isThrownBy(() -> cartItem.decreaseQuantity(-1));
    }

    @Test
    void shouldThrowWhenProductIsNull() {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> new CartItem(null, 1));
    }

    @Test
    void shouldRecalculateTotalPriceAfterQuantityChange() {
        cartItem.increaseQuantity(3); // now 5
        assertThat(cartItem.calculateTotalPrice()).isEqualByComparingTo(new BigDecimal("4000"));
    }
}