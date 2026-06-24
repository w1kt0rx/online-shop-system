package integration;

import customer.dto.*;
import customer.repository.impl.InMemoryCustomerRepository;
import customer.service.CustomerService;
import exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

class CustomerAuthIT {

    private CustomerService customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerService(new InMemoryCustomerRepository());
    }

    @Test
    void register_persistsCustomerWithHashedPassword() {
        CustomerDto dto = customerService.createCustomer(
                new CreateCustomerRequest("Jan Kowalski", "jan@example.com", "Password123"));

        assertThat(dto.id()).isPositive();
        assertThat(dto.name()).isEqualTo("Jan Kowalski");
        assertThat(dto.email()).isEqualTo("jan@example.com");
        assertThat(dto.cart()).isNotNull();
        assertThat(dto.cart().items()).isEmpty();

        assertThat(dto.toString()).doesNotContain("Password123");
    }

    @Test
    void register_emailNormalisedToLowercase() {
        CustomerDto dto = customerService.createCustomer(
                new CreateCustomerRequest("Alice", "ALICE@EXAMPLE.COM", "Password123"));

        assertThat(dto.email()).isEqualTo("alice@example.com");
    }

    @Test
    void register_duplicateEmailThrows_secondCustomerNotPersisted() {
        customerService.createCustomer(
                new CreateCustomerRequest("First", "shared@example.com", "Password123"));

        assertThatExceptionOfType(EmailAlreadyInUseException.class)
                .isThrownBy(() -> customerService.createCustomer(
                        new CreateCustomerRequest("Second", "shared@example.com", "Other123")))
                .withMessageContaining("shared@example.com");

        assertThat(customerService.getAllCustomers()).hasSize(1);
    }

    @Test
    void register_duplicateEmailCaseInsensitive() {
        customerService.createCustomer(
                new CreateCustomerRequest("First", "user@example.com", "Password123"));

        assertThatExceptionOfType(EmailAlreadyInUseException.class)
                .isThrownBy(() -> customerService.createCustomer(
                        new CreateCustomerRequest("Second", "USER@EXAMPLE.COM", "Other123")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-an-email", "@no-local.com", "missing-at.com", "a b@c.com"})
    void register_invalidEmailFormatThrows(String badEmail) {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> customerService.createCustomer(
                        new CreateCustomerRequest("Test", badEmail, "Password123")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"short", "1234567", "7chars!"})
    void register_passwordTooShortThrows(String shortPwd) {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> customerService.createCustomer(
                        new CreateCustomerRequest("Test", "test@example.com", shortPwd)));
    }

    @Test
    void register_blankNameThrows() {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> customerService.createCustomer(
                        new CreateCustomerRequest("   ", "test@example.com", "Password123")));
    }

    @Test
    void register_multipleValidationErrorsAggregated() {
        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> customerService.createCustomer(
                        new CreateCustomerRequest("", "bad", "")))
                .withMessageContaining("name")
                .withMessageContaining("mail");
    }

    @Test
    void login_correctCredentials_returnsCustomerDto() {
        CustomerDto registered = customerService.createCustomer(
                new CreateCustomerRequest("Piotr Wiśniewski", "piotr@example.com", "Secure456"));

        CustomerDto loggedIn = customerService.login(
                new LoginRequest("piotr@example.com", "Secure456"));

        assertThat(loggedIn.id()).isEqualTo(registered.id());
        assertThat(loggedIn.name()).isEqualTo("Piotr Wiśniewski");
        assertThat(loggedIn.email()).isEqualTo("piotr@example.com");
        assertThat(loggedIn.toString()).doesNotContain("Secure456");
    }

    @Test
    void login_emailCaseInsensitive() {
        customerService.createCustomer(
                new CreateCustomerRequest("User", "user@example.com", "Password123"));

        CustomerDto loggedIn = customerService.login(
                new LoginRequest("USER@EXAMPLE.COM", "Password123"));

        assertThat(loggedIn.email()).isEqualTo("user@example.com");
    }

    @Test
    void login_wrongPassword_sameExceptionAsUnknownEmail_noEnumeration() {
        customerService.createCustomer(
                new CreateCustomerRequest("User", "user@example.com", "Password123"));

        assertThatExceptionOfType(InvalidCredentialsException.class)
                .isThrownBy(() -> customerService.login(
                        new LoginRequest("user@example.com", "WrongPassword")));

        assertThatExceptionOfType(InvalidCredentialsException.class)
                .isThrownBy(() -> customerService.login(
                        new LoginRequest("ghost@example.com", "Password123")));
    }


    @Test
    void updateCustomer_nameAndEmailChanged() {
        CustomerDto original = customerService.createCustomer(
                new CreateCustomerRequest("Old Name", "old@example.com", "Password123"));

        CustomerDto updated = customerService.updateCustomer(original.id(),
                new UpdateCustomerRequest("New Name", "new@example.com"));

        assertThat(updated.id()).isEqualTo(original.id());
        assertThat(updated.name()).isEqualTo("New Name");
        assertThat(updated.email()).isEqualTo("new@example.com");
    }

    @Test
    void updateCustomer_sameEmailAllowed_noConflict() {
        CustomerDto customer = customerService.createCustomer(
                new CreateCustomerRequest("Alice", "alice@example.com", "Password123"));

        CustomerDto updated = customerService.updateCustomer(customer.id(),
                new UpdateCustomerRequest("Alice Updated", "alice@example.com"));

        assertThat(updated.name()).isEqualTo("Alice Updated");
        assertThat(updated.email()).isEqualTo("alice@example.com");
    }

    @Test
    void updateCustomer_emailTakenByAnotherCustomer_throws() {
        customerService.createCustomer(
                new CreateCustomerRequest("Alice", "alice@example.com", "Password123"));
        CustomerDto bob = customerService.createCustomer(
                new CreateCustomerRequest("Bob", "bob@example.com", "Password123"));

        assertThatExceptionOfType(EmailAlreadyInUseException.class)
                .isThrownBy(() -> customerService.updateCustomer(bob.id(),
                        new UpdateCustomerRequest("Bob", "alice@example.com")));
    }

    @Test
    void updateCustomer_nonExistentId_throws() {
        assertThatExceptionOfType(CustomerNotFoundException.class)
                .isThrownBy(() -> customerService.updateCustomer(999L,
                        new UpdateCustomerRequest("X", "x@example.com")));
    }

    @Test
    void changePassword_correctCurrentPassword_succeeds() {
        CustomerDto customer = customerService.createCustomer(
                new CreateCustomerRequest("User", "user@example.com", "OldPass123"));

        customerService.changePassword(customer.id(),
                new ChangePasswordRequest("OldPass123", "NewPass456"));

        assertThatExceptionOfType(InvalidCredentialsException.class)
                .isThrownBy(() -> customerService.login(
                        new LoginRequest("user@example.com", "OldPass123")));

        CustomerDto loggedIn = customerService.login(
                new LoginRequest("user@example.com", "NewPass456"));
        assertThat(loggedIn.id()).isEqualTo(customer.id());
    }

    @Test
    void changePassword_wrongCurrentPassword_throws_passwordUnchanged() {
        CustomerDto customer = customerService.createCustomer(
                new CreateCustomerRequest("User", "user@example.com", "OldPass123"));

        assertThatExceptionOfType(InvalidCredentialsException.class)
                .isThrownBy(() -> customerService.changePassword(customer.id(),
                        new ChangePasswordRequest("WrongPass", "NewPass456")));

        assertThatNoException()
                .isThrownBy(() -> customerService.login(
                        new LoginRequest("user@example.com", "OldPass123")));
    }

    @Test
    void getAllCustomers_returnsAll() {
        customerService.createCustomer(new CreateCustomerRequest("A", "a@example.com", "Password123"));
        customerService.createCustomer(new CreateCustomerRequest("B", "b@example.com", "Password123"));
        customerService.createCustomer(new CreateCustomerRequest("C", "c@example.com", "Password123"));

        List<CustomerDto> all = customerService.getAllCustomers();

        assertThat(all).hasSize(3);
        assertThat(all).extracting(CustomerDto::name)
                .containsExactlyInAnyOrder("A", "B", "C");
    }

    @Test
    void deleteCustomer_removedAndThrowsOnSubsequentFetch() {
        CustomerDto customer = customerService.createCustomer(
                new CreateCustomerRequest("Temp", "temp@example.com", "Password123"));

        customerService.deleteCustomer(customer.id());

        assertThatExceptionOfType(CustomerNotFoundException.class)
                .isThrownBy(() -> customerService.getCustomerById(customer.id()));
        assertThat(customerService.getAllCustomers()).isEmpty();
    }

    @Test
    void getCustomerById_throwsForNonExistentId() {
        assertThatExceptionOfType(CustomerNotFoundException.class)
                .isThrownBy(() -> customerService.getCustomerById(999L));
    }
}