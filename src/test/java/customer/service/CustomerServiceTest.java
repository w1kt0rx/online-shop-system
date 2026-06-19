package customer.service;

import customer.dto.ChangePasswordRequest;
import customer.dto.CreateCustomerRequest;
import customer.dto.CustomerDto;
import customer.dto.LoginRequest;
import customer.dto.UpdateCustomerRequest;
import customer.model.Customer;
import customer.repository.CustomerRepository;
import exception.CustomerNotFoundException;
import exception.EmailAlreadyInUseException;
import exception.InvalidCredentialsException;
import exception.InvalidProductException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private static final String EMAIL = "jan@example.com";
    private static final String PASSWORD = "Password123";

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void shouldCreateCustomer() {
        when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(customerRepository.getNextId()).thenReturn(1L);
        Customer saved = new Customer(1L, "Jan Kowalski", EMAIL, PASSWORD);
        when(customerRepository.save(any())).thenReturn(saved);

        CustomerDto result = customerService.createCustomer(
                new CreateCustomerRequest("Jan Kowalski", EMAIL, PASSWORD));

        assertEquals(1L, result.id());
        assertEquals("Jan Kowalski", result.name());
        assertEquals(EMAIL, result.email());
        assertNotNull(result.cart());
        verify(customerRepository).save(any());
    }

    @Test
    void shouldThrowWhenCreatingCustomerWithBlankName() {
        when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(customerRepository.getNextId()).thenReturn(1L);

        assertThrows(InvalidProductException.class,
                () -> customerService.createCustomer(new CreateCustomerRequest("", EMAIL, PASSWORD)));
    }

    @Test
    void shouldThrowWhenEmailAlreadyRegistered() {
        when(customerRepository.findByEmail(EMAIL))
                .thenReturn(Optional.of(new Customer(1L, "Existing", EMAIL, PASSWORD)));

        assertThrows(EmailAlreadyInUseException.class,
                () -> customerService.createCustomer(
                        new CreateCustomerRequest("New Name", EMAIL, PASSWORD)));
        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldLoginWithCorrectCredentials() {
        Customer customer = new Customer(1L, "Jan Kowalski", EMAIL, PASSWORD);
        when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));

        CustomerDto result = customerService.login(new LoginRequest(EMAIL, PASSWORD));

        assertEquals(1L, result.id());
    }

    @Test
    void shouldThrowWhenLoggingInWithUnknownEmail() {
        when(customerRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> customerService.login(new LoginRequest("unknown@example.com", PASSWORD)));
    }

    @Test
    void shouldThrowWhenLoggingInWithWrongPassword() {
        Customer customer = new Customer(1L, "Jan Kowalski", EMAIL, PASSWORD);
        when(customerRepository.findByEmail(EMAIL)).thenReturn(Optional.of(customer));

        assertThrows(InvalidCredentialsException.class,
                () -> customerService.login(new LoginRequest(EMAIL, "wrongPassword")));
    }

    @Test
    void shouldGetCustomerById() {
        Customer customer = new Customer(1L, "Anna Nowak", "anna@example.com", PASSWORD);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CustomerDto result = customerService.getCustomerById(1L);

        assertEquals(1L, result.id());
        assertEquals("Anna Nowak", result.name());
    }

    @Test
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.getCustomerById(99L));
    }

    @Test
    void shouldGetAllCustomers() {
        when(customerRepository.getAll()).thenReturn(List.of(
                new Customer(1L, "Jan Kowalski", "jan@example.com", PASSWORD),
                new Customer(2L, "Anna Nowak", "anna@example.com", PASSWORD)
        ));

        List<CustomerDto> result = customerService.getAllCustomers();

        assertEquals(2, result.size());
    }

    @Test
    void shouldUpdateCustomerNameAndEmail() {
        Customer customer = new Customer(1L, "Jan Kowalski", EMAIL, PASSWORD);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findByEmail("jan.nowy@example.com")).thenReturn(Optional.empty());
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CustomerDto result = customerService.updateCustomer(1L,
                new UpdateCustomerRequest("Jan Nowak", "jan.nowy@example.com"));

        assertEquals("Jan Nowak", result.name());
        assertEquals("jan.nowy@example.com", result.email());
    }

    @Test
    void shouldAllowUpdateKeepingSameEmail() {
        Customer customer = new Customer(1L, "Jan Kowalski", EMAIL, PASSWORD);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CustomerDto result = customerService.updateCustomer(1L,
                new UpdateCustomerRequest("Jan Nowak", EMAIL));

        assertEquals(EMAIL, result.email());
        verify(customerRepository, never()).findByEmail(EMAIL);
    }

    @Test
    void shouldThrowWhenUpdatingToEmailAlreadyTakenByAnotherCustomer() {
        Customer customer = new Customer(1L, "Jan Kowalski", EMAIL, PASSWORD);
        Customer other = new Customer(2L, "Anna Nowak", "anna@example.com", PASSWORD);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.findByEmail("anna@example.com")).thenReturn(Optional.of(other));

        assertThrows(EmailAlreadyInUseException.class,
                () -> customerService.updateCustomer(1L,
                        new UpdateCustomerRequest("Jan Nowak", "anna@example.com")));
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentCustomer() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.updateCustomer(99L,
                        new UpdateCustomerRequest("Jan", EMAIL)));
    }

    @Test
    void shouldChangePasswordWhenCurrentPasswordIsCorrect() {
        Customer customer = new Customer(1L, "Jan Kowalski", EMAIL, PASSWORD);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        customerService.changePassword(1L, new ChangePasswordRequest(PASSWORD, "NewPassword456"));

        assertTrue(customer.checkPassword("NewPassword456"));
    }

    @Test
    void shouldThrowWhenChangingPasswordWithWrongCurrentPassword() {
        Customer customer = new Customer(1L, "Jan Kowalski", EMAIL, PASSWORD);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        assertThrows(InvalidCredentialsException.class,
                () -> customerService.changePassword(1L,
                        new ChangePasswordRequest("wrongPassword", "NewPassword456")));
        verify(customerRepository, never()).save(any());
    }


    @Test
    void shouldDeleteCustomer() {
        Customer customer = new Customer(1L, "Jan Kowalski", EMAIL, PASSWORD);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        customerService.deleteCustomer(1L);

        verify(customerRepository).delete(1L);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentCustomer() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.deleteCustomer(99L));
    }
}