package customer.model;

import cart.model.Cart;
import exception.InvalidProductException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class CustomerTest {

    @Test
    void shouldCreateCustomerWithCorrectData() {
        Customer customer = new Customer(1L, "Jan Kowalski");

        assertThat(customer.getId()).isEqualTo(1L);
        assertThat(customer.getName()).isEqualTo("Jan Kowalski");
        assertThat(customer.getCart()).isNotNull();
        assertThat(customer.getCart().isEmpty()).isTrue();
    }

    @Test
    void shouldCreateCustomerWithEmptyCartByDefault() {
        Customer customer = new Customer(1L, "Test User");
        Cart cart = customer.getCart();

        assertThat(cart).isNotNull();
        assertThat(cart.getCartItems()).isEmpty();
    }

    @Test
    void shouldUpdateNameSuccessfully() {
        Customer customer = new Customer(1L, "Jan Kowalski");
        customer.updateName("Anna Nowak");

        assertThat(customer.getName()).isEqualTo("Anna Nowak");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void shouldThrowWhenNameIsBlankOrNull(String name) {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> new Customer(1L, name));
    }

    @Test
    void shouldThrowWhenIdIsNull() {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> new Customer(null, "Jan"));
    }

    @Test
    void shouldThrowWhenIdIsNegative() {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> new Customer(-1L, "Jan"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldThrowWhenUpdatingToBlankOrNullName(String name) {
        Customer customer = new Customer(1L, "Jan Kowalski");

        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> customer.updateName(name));
    }

    @Test
    void shouldCollectMultipleValidationErrorsOnCreation() {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> new Customer(null, ""))
                .withMessageContaining("Customer id cannot be null or negative")
                .withMessageContaining("Customer name cannot be blank");
    }
}
