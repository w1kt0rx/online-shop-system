package product.repository.smartphone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.model.smartphone.Smartphone;
import product.model.smartphone.configuration.SmartphoneConfiguration;
import product.repository.impl.InMemorySmartphoneRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemorySmartphoneRepositoryTest {

    private InMemorySmartphoneRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemorySmartphoneRepository();
    }

    @Test
    void shouldSaveSmartphone() {
        Smartphone smartphone = new Smartphone(
                1L, "iPhone 15", BigDecimal.valueOf(4000), 5, new SmartphoneConfiguration()
        );

        repository.save(smartphone);

        assertEquals(1, repository.getAll().size());
    }

    @Test
    void shouldFindSmartphoneById() {
        Smartphone smartphone = new Smartphone(
                1L, "iPhone 15", BigDecimal.valueOf(4000), 5, new SmartphoneConfiguration()
        );

        repository.save(smartphone);
        final var result = repository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("iPhone 15", result.get().getName());
    }

    @Test
    void shouldReturnEmptyWhenSmartphoneNotFound() {
        final var result = repository.findById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeleteSmartphone() {
        Smartphone smartphone = new Smartphone(
                1L, "iPhone 15", BigDecimal.valueOf(4000), 5, new SmartphoneConfiguration()
        );

        repository.save(smartphone);
        repository.delete(1L);

        assertTrue(repository.findById(1L).isEmpty());
    }

    @Test
    void shouldReturnAllSmartphones() {
        repository.save(new Smartphone(null, "iPhone 15", BigDecimal.valueOf(4000), 5, new SmartphoneConfiguration()));
        repository.save(new Smartphone(null, "Samsung S24", BigDecimal.valueOf(3500), 10, new SmartphoneConfiguration()));

        assertEquals(2, repository.getAll().size());
    }

    @Test
    void shouldOverwriteWhenSavingWithSameId() {
        Smartphone original = new Smartphone(1L, "iPhone 15", BigDecimal.valueOf(4000), 5, new SmartphoneConfiguration());
        Smartphone updated = new Smartphone(1L, "iPhone 15 Pro", BigDecimal.valueOf(5000), 3, new SmartphoneConfiguration());

        repository.save(original);
        repository.save(updated);

        assertEquals(1, repository.getAll().size());
        assertEquals("iPhone 15 Pro", repository.findById(1L).get().getName());
    }
}
