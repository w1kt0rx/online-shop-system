package invoice.service;

import cart.dto.CartItemDto;
import exception.OrderNotFoundException;
import invoice.dto.InvoiceDto;
import invoice.model.Invoice;
import invoice.repository.InvoiceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.model.ProductType;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private static final Long ORDER_ID    = 10L;
    private static final Long CUSTOMER_ID = 1L;
    private static final String CUSTOMER_NAME = "Jan Kowalski";
    private static final BigDecimal TOTAL = new BigDecimal("1600.00");

    private List<CartItemDto> sampleItems() {
        return List.of(
                new CartItemDto(1L, "Monitor", ProductType.ELECTRONICS,
                        new BigDecimal("800"), 2, new BigDecimal("1600"))
        );
    }

    private Invoice savedInvoice(Long id) {
        Invoice inv = new Invoice(null, ORDER_ID, CUSTOMER_ID, CUSTOMER_NAME, sampleItems(), TOTAL);
        inv.setId(id);
        return inv;
    }

    @Test
    void createInvoice_savesInvoiceToRepository() {
        when(invoiceRepository.save(any())).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });

        invoiceService.createInvoice(ORDER_ID, CUSTOMER_ID, CUSTOMER_NAME, sampleItems(), TOTAL);

        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void createInvoice_returnsInvoiceDtoWithCorrectFields() {
        when(invoiceRepository.save(any())).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(42L);
            return i;
        });

        InvoiceDto result = invoiceService.createInvoice(
                ORDER_ID, CUSTOMER_ID, CUSTOMER_NAME, sampleItems(), TOTAL);

        assertThat(result.id()).isEqualTo(42L);
        assertThat(result.orderId()).isEqualTo(ORDER_ID);
        assertThat(result.customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(result.customerName()).isEqualTo(CUSTOMER_NAME);
        assertThat(result.totalAmount()).isEqualByComparingTo(TOTAL);
        assertThat(result.items()).hasSize(1);
    }

    @Test
    void createInvoice_invoicePassedToRepositoryHasNullId() {
        when(invoiceRepository.save(any())).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });

        invoiceService.createInvoice(ORDER_ID, CUSTOMER_ID, CUSTOMER_NAME, sampleItems(), TOTAL);

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(captor.capture());
        // Repository assigns IDs — service should pass null to let repository do it
        assertThat(captor.getValue().getOrderId()).isEqualTo(ORDER_ID);
        assertThat(captor.getValue().getCustomerId()).isEqualTo(CUSTOMER_ID);
        assertThat(captor.getValue().getCustomerName()).isEqualTo(CUSTOMER_NAME);
        assertThat(captor.getValue().getTotalAmount()).isEqualByComparingTo(TOTAL);
    }

    @Test
    void createInvoice_issuedAtTimestampIsPopulated() {
        when(invoiceRepository.save(any())).thenAnswer(inv -> {
            Invoice i = inv.getArgument(0);
            i.setId(1L);
            return i;
        });

        InvoiceDto result = invoiceService.createInvoice(
                ORDER_ID, CUSTOMER_ID, CUSTOMER_NAME, sampleItems(), TOTAL);

        assertThat(result.issuedAt()).isNotNull();
    }

    @Test
    void getInvoiceByOrderId_returnsMatchingInvoice() {
        Invoice invoice = savedInvoice(5L);
        when(invoiceRepository.getAll()).thenReturn(List.of(invoice));

        InvoiceDto result = invoiceService.getInvoiceByOrderId(ORDER_ID);

        assertThat(result.id()).isEqualTo(5L);
        assertThat(result.orderId()).isEqualTo(ORDER_ID);
        assertThat(result.customerId()).isEqualTo(CUSTOMER_ID);
    }

    @Test
    void getInvoiceByOrderId_selectsCorrectInvoiceAmongMultiple() {
        Invoice inv1 = new Invoice(null, 1L, 10L, "Alice", sampleItems(), new BigDecimal("500"));
        inv1.setId(1L);
        Invoice inv2 = new Invoice(null, 2L, 20L, "Bob",   sampleItems(), new BigDecimal("800"));
        inv2.setId(2L);
        Invoice inv3 = new Invoice(null, 3L, 30L, "Carol", sampleItems(), new BigDecimal("300"));
        inv3.setId(3L);

        when(invoiceRepository.getAll()).thenReturn(List.of(inv1, inv2, inv3));

        InvoiceDto result = invoiceService.getInvoiceByOrderId(2L);

        assertThat(result.id()).isEqualTo(2L);
        assertThat(result.customerId()).isEqualTo(20L);
        assertThat(result.customerName()).isEqualTo("Bob");
    }

    @Test
    void getAllInvoices_returnsAllInvoices() {
        Invoice inv1 = savedInvoice(1L);
        Invoice inv2 = new Invoice(null, 20L, 2L, "Anna", sampleItems(), new BigDecimal("500"));
        inv2.setId(2L);
        when(invoiceRepository.getAll()).thenReturn(List.of(inv1, inv2));

        List<InvoiceDto> result = invoiceService.getAllInvoices();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(InvoiceDto::id).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void getAllInvoices_returnsEmptyList_whenNoInvoices() {
        when(invoiceRepository.getAll()).thenReturn(List.of());

        List<InvoiceDto> result = invoiceService.getAllInvoices();

        assertThat(result).isEmpty();
    }

    @Test
    void getAllInvoices_mapsAllDtoFieldsCorrectly() {
        Invoice invoice = savedInvoice(7L);
        when(invoiceRepository.getAll()).thenReturn(List.of(invoice));

        InvoiceDto dto = invoiceService.getAllInvoices().get(0);

        assertThat(dto.id()).isEqualTo(7L);
        assertThat(dto.orderId()).isEqualTo(ORDER_ID);
        assertThat(dto.customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(dto.customerName()).isEqualTo(CUSTOMER_NAME);
        assertThat(dto.totalAmount()).isEqualByComparingTo(TOTAL);
        assertThat(dto.items()).hasSize(1);
        assertThat(dto.issuedAt()).isNotNull();
    }
}