package discount.repository;

import discount.model.Discount;
import discount.model.DiscountType;
import discount.repository.impl.InMemoryDiscountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryDiscountRepositoryTest {

    private InMemoryDiscountRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryDiscountRepository();
    }

    private Discount buildDiscount(Long id, String code) {
        return new Discount(id, code, "desc", DiscountType.PERCENTAGE,
                new BigDecimal("10"), null,
                ZonedDateTime.now().minusDays(1),
                ZonedDateTime.now().plusDays(7));
    }

    @Test
    void shouldSaveAndFindById() {
        Discount discount = buildDiscount(1L, "CODE10");
        repository.save(discount);

        Optional<Discount> result = repository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getCode()).isEqualTo("CODE10");
    }

    @Test
    void shouldFindByCodeCaseInsensitive() {
        repository.save(buildDiscount(1L, "SUMMER10"));

        assertThat(repository.findByCode("summer10")).isPresent();
        assertThat(repository.findByCode("SUMMER10")).isPresent();
        assertThat(repository.findByCode("Summer10")).isPresent();
    }

    @Test
    void shouldReturnEmptyWhenCodeNotFound() {
        Optional<Discount> result = repository.findByCode("NONEXISTENT");
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenIdNotFound() {
        Optional<Discount> result = repository.findById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void shouldReturnAllSavedDiscounts() {
        repository.save(buildDiscount(1L, "CODE1"));
        repository.save(buildDiscount(2L, "CODE2"));
        repository.save(buildDiscount(3L, "CODE3"));

        List<Discount> all = repository.getAll();

        assertThat(all).hasSize(3);
    }

    @Test
    void shouldDeleteById() {
        repository.save(buildDiscount(1L, "TO_DELETE"));
        repository.delete(1L);

        assertThat(repository.findById(1L)).isEmpty();
    }

    @Test
    void shouldIncrementNextId() {
        Long first = repository.getNextId();
        Long second = repository.getNextId();

        assertThat(second).isEqualTo(first + 1);
    }

    @Test
    void shouldOverwriteExistingDiscountOnSave() {
        Discount original = buildDiscount(1L, "ORIGINAL");
        repository.save(original);

        Discount updated = buildDiscount(1L, "UPDATED");
        repository.save(updated);

        assertThat(repository.findByCode("UPDATED")).isPresent();
        assertThat(repository.getAll()).hasSize(1);
    }
}