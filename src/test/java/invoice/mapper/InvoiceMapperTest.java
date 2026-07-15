package invoice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import cart.dto.CartItemDto;
import invoice.dto.InvoiceDto;
import invoice.model.Invoice;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import product.model.ProductType;

class InvoiceMapperTest {

    @Test
    void shouldMapInvoiceToDtoWithAllFields() {
        List<CartItemDto> items = List.of(
            new CartItemDto(1L, "Monitor", ProductType.ELECTRONICS, new BigDecimal("800"), 2, new BigDecimal("1600"))
        );
        Invoice invoice = new Invoice(1L, 10L, 5L, "Jan Kowalski", items, new BigDecimal("1600"));

        InvoiceDto dto = InvoiceMapper.toDto(invoice);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.orderId()).isEqualTo(10L);
        assertThat(dto.customerId()).isEqualTo(5L);
        assertThat(dto.customerName()).isEqualTo("Jan Kowalski");
        assertThat(dto.totalAmount()).isEqualByComparingTo(new BigDecimal("1600"));
        assertThat(dto.issuedAt()).isEqualTo(invoice.getIssuedAt());
        assertThat(dto.items()).hasSize(1);
    }

    @Test
    void shouldMapInvoiceWithEmptyItems() {
        Invoice invoice = new Invoice(2L, 1L, 1L, "Test User", List.of(), BigDecimal.ZERO);

        InvoiceDto dto = InvoiceMapper.toDto(invoice);

        assertThat(dto.items()).isEmpty();
        assertThat(dto.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void shouldMapInvoiceWithMultipleItems() {
        List<CartItemDto> items = List.of(
            new CartItemDto(1L, "Monitor", ProductType.ELECTRONICS, new BigDecimal("800"), 1, new BigDecimal("800")),
            new CartItemDto(2L, "Keyboard", ProductType.ELECTRONICS, new BigDecimal("150"), 2, new BigDecimal("300"))
        );
        Invoice invoice = new Invoice(3L, 5L, 2L, "Anna Nowak", items, new BigDecimal("1100"));

        InvoiceDto dto = InvoiceMapper.toDto(invoice);

        assertThat(dto.items()).hasSize(2);
        assertThat(dto.totalAmount()).isEqualByComparingTo(new BigDecimal("1100"));
    }
}
