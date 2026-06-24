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

    private static final String VALID_EMAIL = "jan@example.com";
    private static final String VALID_PASSWORD = "Password123";

    @Test
    void shouldCreateCustomerWithCorrectData() {
        Customer customer = new Customer(1L, "Jan Kowalski", VALID_EMAIL, VALID_PASSWORD);

        assertThat(customer.getId()).isEqualTo(1L);
        assertThat(customer.getName()).isEqualTo("Jan Kowalski");
        assertThat(customer.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(customer.getCart()).isNotNull();
        assertThat(customer.getCart().isEmpty()).isTrue();
    }

    @Test
    void shouldNormalizeEmailToLowercase() {
        Customer customer = new Customer(1L, "Jan Kowalski", "JAN@EXAMPLE.COM", VALID_PASSWORD);

        assertThat(customer.getEmail()).isEqualTo("jan@example.com");
    }

    @Test
    void shouldNeverExposePasswordInToString() {
        Customer customer = new Customer(1L, "Jan Kowalski", VALID_EMAIL, VALID_PASSWORD);

        assertThat(customer.toString()).doesNotContain(VALID_PASSWORD);
        assertThat(customer.toString()).doesNotContain("passwordHash");
    }

    @Test
    void shouldHashPasswordRatherThanStoringPlaintext() {
        Customer customer = new Customer(1L, "Jan Kowalski", VALID_EMAIL, VALID_PASSWORD);

        assertThat(customer.getPasswordHash()).isNotEqualTo(VALID_PASSWORD);
        assertThat(customer.getPasswordHash()).contains(":");
    }

    @Test
    void shouldVerifyCorrectPassword() {
        Customer customer = new Customer(1L, "Jan Kowalski", VALID_EMAIL, VALID_PASSWORD);

        assertThat(customer.checkPassword(VALID_PASSWORD)).isTrue();
    }

    @Test
    void shouldRejectIncorrectPassword() {
        Customer customer = new Customer(1L, "Jan Kowalski", VALID_EMAIL, VALID_PASSWORD);

        assertThat(customer.checkPassword("wrongPassword")).isFalse();
    }

    @Test
    void shouldCreateCustomerWithEmptyCartByDefault() {
        Customer customer = new Customer(1L, "Test User", VALID_EMAIL, VALID_PASSWORD);
        Cart cart = customer.getCart();

        assertThat(cart).isNotNull();
        assertThat(cart.getCartItems()).isEmpty();
    }

    @Test
    void shouldUpdateNameSuccessfully() {
        Customer customer = new Customer(1L, "Jan Kowalski", VALID_EMAIL, VALID_PASSWORD);
        customer.updateName("Anna Nowak");

        assertThat(customer.getName()).isEqualTo("Anna Nowak");
    }

    @Test
    void shouldUpdateEmailSuccessfully() {
        Customer customer = new Customer(1L, "Jan Kowalski", VALID_EMAIL, VALID_PASSWORD);
        customer.updateEmail("new@example.com");

        assertThat(customer.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    void shouldChangePasswordSuccessfully() {
        Customer customer = new Customer(1L, "Jan Kowalski", VALID_EMAIL, VALID_PASSWORD);
        customer.changePassword("NewPassword456");

        assertThat(customer.checkPassword("NewPassword456")).isTrue();
        assertThat(customer.checkPassword(VALID_PASSWORD)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void shouldThrowWhenNameIsBlankOrNull(String name) {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> new Customer(1L, name, VALID_EMAIL, VALID_PASSWORD));
    }

    @Test
    void shouldThrowWhenIdIsNegative() {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> new Customer(-1L, "Jan", VALID_EMAIL, VALID_PASSWORD));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-an-email", "missing-at.com", "@no-local.com", "spaces in@email.com"})
    void shouldThrowWhenEmailFormatIsInvalid(String invalidEmail) {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> new Customer(1L, "Jan", invalidEmail, VALID_PASSWORD));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowWhenEmailIsBlankOrNull(String email) {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> new Customer(1L, "Jan", email, VALID_PASSWORD));
    }

    @ParameterizedTest
    @ValueSource(strings = {"short", "1234567"})
    void shouldThrowWhenPasswordIsTooShort(String shortPassword) {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> new Customer(1L, "Jan", VALID_EMAIL, shortPassword));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void shouldThrowWhenPasswordIsBlankOrNull(String password) {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> new Customer(1L, "Jan", VALID_EMAIL, password));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void shouldThrowWhenUpdatingToBlankOrNullName(String name) {
        Customer customer = new Customer(1L, "Jan Kowalski", VALID_EMAIL, VALID_PASSWORD);

        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> customer.updateName(name));
    }

    @Test
    void shouldThrowWhenUpdatingToInvalidEmail() {
        Customer customer = new Customer(1L, "Jan Kowalski", VALID_EMAIL, VALID_PASSWORD);

        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> customer.updateEmail("not-an-email"));
    }

    @Test
    void shouldThrowWhenChangingToTooShortPassword() {
        Customer customer = new Customer(1L, "Jan Kowalski", VALID_EMAIL, VALID_PASSWORD);

        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> customer.changePassword("short"));
    }

    @Test
    void shouldCollectMultipleValidationErrorsOnCreation() {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> new Customer(null, "", "", ""))
                .withMessageContaining("Customer name cannot be blank")
                .withMessageContaining("Email cannot be blank")
                .withMessageContaining("Password cannot be blank");
    }
}