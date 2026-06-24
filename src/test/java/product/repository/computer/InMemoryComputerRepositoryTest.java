package product.repository.computer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.model.computer.Computer;
import product.model.computer.configuration.ComputerConfiguration;
import product.repository.impl.InMemoryComputerRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryComputerRepositoryTest {

    private InMemoryComputerRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryComputerRepository();
    }

    @Test
    void shouldSaveComputer() {

        Computer computer = new Computer(
                1L,
                "PC",
                BigDecimal.valueOf(3000),
                5,
                new ComputerConfiguration()
        );

        repository.save(computer);

        assertEquals(
                1,
                repository.getAll().size()
        );
    }

    @Test
    void shouldFindComputerById() {

        Computer computer = new Computer(
                1L,
                "PC",
                BigDecimal.valueOf(3000),
                5,
                new ComputerConfiguration()
        );

        repository.save(computer);

        final var result =
                repository.findById(1L);

        assertTrue(result.isPresent());
    }

    @Test
    void shouldDeleteComputer() {

        Computer computer = new Computer(
                1L,
                "PC",
                BigDecimal.valueOf(3000),
                5,
                new ComputerConfiguration()
        );

        repository.save(computer);

        repository.delete(1L);

        assertTrue(
                repository.findById(1L).isEmpty()
        );
    }
}
