package invoice.model;

import cart.dto.CartItemDto;
import org.junit.jupiter.api.Test;
import product.model.ProductType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvoiceTest {

    private List<CartItemDto> sampleItems() {
        return List.of(
                new CartItemDto(1L, "Monitor", ProductType.ELECTRONICS,
                        new BigDecimal("800"), 2, new BigDecimal("1600"))
        );
    }

    @Test
    void shouldCreateInvoiceWithCorrectData() {
        Invoice invoice = new Invoice(1L, 10L, 5L, "Jan Kowalski",
                sampleItems(), new BigDecimal("1600"));

        assertThat(invoice.getId()).isEqualTo(1L);
        assertThat(invoice.getOrderId()).isEqualTo(10L);
        assertThat(invoice.getCustomerId()).isEqualTo(5L);
        assertThat(invoice.getCustomerName()).isEqualTo("Jan Kowalski");
        assertThat(invoice.getTotalAmount()).isEqualByComparingTo(new BigDecimal("1600"));
        assertThat(invoice.getIssuedAt()).isNotNull();
    }

    @Test
    void shouldSetIssuedAtTimestampOnCreation() {
        ZonedDateTime before = ZonedDateTime.now().minusSeconds(1);
        Invoice invoice = new Invoice(1L, 1L, 1L, "Test", sampleItems(), BigDecimal.TEN);
        ZonedDateTime after = ZonedDateTime.now().plusSeconds(1);

        assertThat(invoice.getIssuedAt()).isAfter(before).isBefore(after);
    }

    @Test
    void shouldHaveImmutableItems() {
        Invoice invoice = new Invoice(1L, 1L, 1L, "Test", sampleItems(), BigDecimal.TEN);

        assertThrows(UnsupportedOperationException.class,
                () -> invoice.getItems().add(sampleItems().get(0)));
    }

    @Test
    void shouldCopyItemsOnCreation() {
        List<CartItemDto> mutableItems = new java.util.ArrayList<>(sampleItems());
        Invoice invoice = new Invoice(1L, 1L, 1L, "Test", mutableItems, BigDecimal.TEN);
        mutableItems.clear();

        assertThat(invoice.getItems()).hasSize(1);
    }

    @Test
    void shouldHandleZeroTotalAmount() {
        Invoice invoice = new Invoice(1L, 1L, 1L, "Test", List.of(), BigDecimal.ZERO);
        assertThat(invoice.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}