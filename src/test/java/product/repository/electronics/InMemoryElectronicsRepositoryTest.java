package product.repository.electronics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.model.electronics.Electronics;
import product.repository.impl.InMemoryElectronicsRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryElectronicsRepositoryTest {

    private InMemoryElectronicsRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryElectronicsRepository();
    }

    @Test
    void shouldSaveElectronics() {
        Electronics electronics = new Electronics(1L, "Monitor 4K", BigDecimal.valueOf(1500), 10);

        repository.save(electronics);

        assertEquals(1, repository.getAll().size());
    }

    @Test
    void shouldFindElectronicsById() {
        Electronics electronics = new Electronics(1L, "Monitor 4K", BigDecimal.valueOf(1500), 10);

        repository.save(electronics);
        final var result = repository.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Monitor 4K", result.get().getName());
    }

    @Test
    void shouldReturnEmptyWhenElectronicsNotFound() {
        final var result = repository.findById(99L);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeleteElectronics() {
        Electronics electronics = new Electronics(1L, "Monitor 4K", BigDecimal.valueOf(1500), 10);

        repository.save(electronics);
        repository.delete(1L);

        assertTrue(repository.findById(1L).isEmpty());
    }

    @Test
    void shouldReturnAllElectronics() {
        repository.save(new Electronics(null, "Monitor 4K", BigDecimal.valueOf(1500), 10));
        repository.save(new Electronics(null, "Keyboard", BigDecimal.valueOf(300), 25));
        repository.save(new Electronics(null,"Headphones", BigDecimal.valueOf(500), 15));

        assertEquals(3, repository.getAll().size());
    }

    @Test
    void shouldOverwriteWhenSavingWithSameId() {
        Electronics original = new Electronics(1L, "Monitor 4K", BigDecimal.valueOf(1500), 10);
        Electronics updated = new Electronics(1L, "Monitor 8K", BigDecimal.valueOf(3000), 5);

        repository.save(original);
        repository.save(updated);

        assertEquals(1, repository.getAll().size());
        assertEquals("Monitor 8K", repository.findById(1L).get().getName());
    }
}
