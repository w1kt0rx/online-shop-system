package invoice.repository;

import cart.dto.CartItemDto;
import invoice.model.Invoice;
import invoice.repository.impl.InMemoryInvoiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.model.ProductType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryInvoiceRepositoryTest {

    private InMemoryInvoiceRepository repository;

    private Invoice buildInvoice(Long id, Long orderId) {
        List<CartItemDto> items = List.of(
                new CartItemDto(1L, "Monitor", ProductType.ELECTRONICS,
                        new BigDecimal("800"), 1, new BigDecimal("800"))
        );
        return new Invoice(id, orderId, 1L, "Jan Kowalski", items, new BigDecimal("800"));
    }

    @BeforeEach
    void setUp() {
        repository = new InMemoryInvoiceRepository();
    }

    @Test
    void shouldSaveAndFindInvoice() {
        Invoice invoice = buildInvoice(1L, 10L);
        repository.save(invoice);

        Optional<Invoice> result = repository.findById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo(10L);
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        assertThat(repository.findById(99L)).isEmpty();
    }

    @Test
    void shouldDeleteInvoice() {
        repository.save(buildInvoice(1L, 10L));
        repository.delete(1L);

        assertThat(repository.findById(1L)).isEmpty();
    }

    @Test
    void shouldReturnAllInvoices() {
        repository.save(buildInvoice(1L, 10L));
        repository.save(buildInvoice(2L, 11L));
        repository.save(buildInvoice(3L, 12L));

        assertThat(repository.getAll()).hasSize(3);
    }

    @Test
    void shouldGenerateSequentialIds() {
        Long first = repository.getNextId();
        Long second = repository.getNextId();

        assertThat(second).isEqualTo(first + 1);
    }

    @Test
    void shouldOverwriteInvoiceWithSameId() {
        repository.save(buildInvoice(1L, 10L));
        repository.save(buildInvoice(1L, 20L));

        assertThat(repository.getAll()).hasSize(1);
        assertThat(repository.findById(1L).get().getOrderId()).isEqualTo(20L);
    }

    @Test
    void shouldReturnEmptyListInitially() {
        assertThat(repository.getAll()).isEmpty();
    }
}