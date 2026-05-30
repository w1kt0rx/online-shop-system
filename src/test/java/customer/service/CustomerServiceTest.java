package customer.service;

import customer.dto.CreateCustomerRequest;
import customer.dto.CustomerDto;
import customer.dto.UpdateCustomerRequest;
import customer.model.Customer;
import customer.repository.CustomerRepository;
import exception.CustomerNotFoundException;
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

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void shouldCreateCustomer() {
        when(customerRepository.getNextId()).thenReturn(1L);
        Customer saved = new Customer(1L, "Jan Kowalski");
        when(customerRepository.save(any())).thenReturn(saved);

        CustomerDto result = customerService.createCustomer(new CreateCustomerRequest("Jan Kowalski"));

        assertEquals(1L, result.id());
        assertEquals("Jan Kowalski", result.name());
        assertNotNull(result.cart());
        verify(customerRepository).save(any());
    }

    @Test
    void shouldThrowWhenCreatingCustomerWithBlankName() {
        when(customerRepository.getNextId()).thenReturn(1L);

        assertThrows(InvalidProductException.class,
                () -> customerService.createCustomer(new CreateCustomerRequest("")));
    }

    @Test
    void shouldGetCustomerById() {
        Customer customer = new Customer(1L, "Anna Nowak");
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
                new Customer(1L, "Jan Kowalski"),
                new Customer(2L, "Anna Nowak")
        ));

        List<CustomerDto> result = customerService.getAllCustomers();

        assertEquals(2, result.size());
    }

    @Test
    void shouldUpdateCustomerName() {
        Customer customer = new Customer(1L, "Jan Kowalski");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CustomerDto result = customerService.updateCustomer(1L, new UpdateCustomerRequest("Jan Nowak"));

        assertEquals("Jan Nowak", result.name());
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentCustomer() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> customerService.updateCustomer(99L, new UpdateCustomerRequest("Jan")));
    }

    @Test
    void shouldDeleteCustomer() {
        Customer customer = new Customer(1L, "Jan Kowalski");
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