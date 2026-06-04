package customer.repository;

import customer.model.Customer;
import customer.repository.impl.InMemoryCustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryCustomerRepositoryTest {

    private InMemoryCustomerRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryCustomerRepository();
    }

    @Test
    void shouldSaveAndFindCustomer() {
        Customer customer = new Customer(1L, "Jan Kowalski");
        repository.save(customer);

        Optional<Customer> result = repository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Jan Kowalski");
    }

    @Test
    void shouldReturnEmptyWhenCustomerNotFound() {
        assertThat(repository.findById(99L)).isEmpty();
    }

    @Test
    void shouldDeleteCustomer() {
        Customer customer = new Customer(1L, "Jan Kowalski");
        repository.save(customer);
        repository.delete(1L);

        assertThat(repository.findById(1L)).isEmpty();
    }

    @Test
    void shouldReturnAllCustomers() {
        repository.save(new Customer(1L, "Jan Kowalski"));
        repository.save(new Customer(2L, "Anna Nowak"));
        repository.save(new Customer(3L, "Piotr Wiśniewski"));

        List<Customer> all = repository.getAll();

        assertThat(all).hasSize(3);
    }

    @Test
    void shouldOverwriteCustomerWithSameId() {
        repository.save(new Customer(1L, "Jan Kowalski"));
        repository.save(new Customer(1L, "Jan Nowak"));

        assertThat(repository.getAll()).hasSize(1);
        assertThat(repository.findById(1L).get().getName()).isEqualTo("Jan Nowak");
    }

    @Test
    void shouldGenerateSequentialIds() {
        Long first = repository.getNextId();
        Long second = repository.getNextId();
        Long third = repository.getNextId();

        assertThat(second).isEqualTo(first + 1);
        assertThat(third).isEqualTo(first + 2);
    }

    @Test
    void shouldReturnEmptyListWhenNoCustomersSaved() {
        assertThat(repository.getAll()).isEmpty();
    }

    @Test
    void shouldNotThrowWhenDeletingNonExistentId() {
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> repository.delete(999L)
        );
    }
}