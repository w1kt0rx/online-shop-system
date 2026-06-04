package order.repository;

import cart.model.Cart;
import exception.EmptyCartException;
import order.validator.OrderValidator;
import org.junit.jupiter.api.Test;
import product.model.electronics.Electronics;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class OrderValidatorTest {

    @Test
    void shouldPassWhenCartHasItems() {
        Cart cart = new Cart();
        cart.addProduct(new Electronics(1L, "Monitor", new BigDecimal("800"), 5), 1);

        assertThatCode(() -> OrderValidator.validateCart(cart))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowWhenCartIsEmpty() {
        Cart cart = new Cart();

        assertThatExceptionOfType(EmptyCartException.class)
                .isThrownBy(() -> OrderValidator.validateCart(cart))
                .withMessageContaining("Cannot place an order with an empty cart");
    }

    @Test
    void shouldThrowWhenCartIsNull() {
        assertThatExceptionOfType(EmptyCartException.class)
                .isThrownBy(() -> OrderValidator.validateCart(null));
    }

    @Test
    void shouldPassAfterItemsAddedToCart() {
        Cart cart = new Cart();
        cart.addProduct(new Electronics(1L, "Keyboard", new BigDecimal("200"), 10), 2);
        cart.addProduct(new Electronics(2L, "Mouse", new BigDecimal("100"), 10), 1);

        assertThatCode(() -> OrderValidator.validateCart(cart))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldThrowAfterCartIsCleared() {
        Cart cart = new Cart();
        cart.addProduct(new Electronics(1L, "Monitor", new BigDecimal("800"), 5), 1);
        cart.clear();

        assertThatExceptionOfType(EmptyCartException.class)
                .isThrownBy(() -> OrderValidator.validateCart(cart));
    }
}