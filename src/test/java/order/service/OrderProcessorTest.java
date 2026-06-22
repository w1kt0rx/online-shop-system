package order.service;

import customer.model.Customer;
import customer.repository.CustomerRepository;
import exception.CustomerNotFoundException;
import exception.EmptyCartException;
import exception.InsufficientStockException;
import exception.OrderNotFoundException;
import invoice.dto.InvoiceDto;
import invoice.model.Invoice;
import invoice.repository.InvoiceRepository;
import order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.model.electronics.Electronics;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderProcessorTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private OrderProcessor orderProcessor;

    private Customer customer;
    private Electronics product;

    @BeforeEach
    void setUp() {
        product = new Electronics(1L, "Monitor", new BigDecimal("800"), 10);
        customer = new Customer(1L, "Jan Kowalski", "wiktor@gmail.com", "Password!123");
        customer.getCart().addProduct(product, 2);
    }

    @Test
    void shouldProcessOrderAndReturnInvoice() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.getNextId()).thenReturn(1L);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.getNextId()).thenReturn(1L);
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        InvoiceDto result = orderProcessor.processOrder(1L);

        assertThat(result).isNotNull();
        assertThat(result.customerId()).isEqualTo(1L);
        assertThat(result.customerName()).isEqualTo("Jan Kowalski");
        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("1600"));
    }

    @Test
    void shouldDecreaseProductStockAfterOrder() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.getNextId()).thenReturn(1L);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.getNextId()).thenReturn(1L);
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderProcessor.processOrder(1L);

        assertThat(product.getQuantity()).isEqualTo(8); // 10 - 2
    }

    @Test
    void shouldClearCartAfterSuccessfulOrder() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.getNextId()).thenReturn(1L);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.getNextId()).thenReturn(1L);
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderProcessor.processOrder(1L);

        assertThat(customer.getCart().isEmpty()).isTrue();
    }

    @Test
    void shouldSaveOrderToRepository() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.getNextId()).thenReturn(1L);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.getNextId()).thenReturn(1L);
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderProcessor.processOrder(1L);

        verify(orderRepository).save(any());
    }

    @Test
    void shouldSaveInvoiceToRepository() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.getNextId()).thenReturn(1L);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.getNextId()).thenReturn(1L);
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderProcessor.processOrder(1L);

        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(CustomerNotFoundException.class)
                .isThrownBy(() -> orderProcessor.processOrder(99L))
                .withMessageContaining("99");
    }

    @Test
    void shouldThrowWhenCartIsEmpty() {
        Customer emptyCustomer = new Customer(2L, "Anna Nowak", "wiktor@gmail.com", "Password!123");
        when(customerRepository.findById(2L)).thenReturn(Optional.of(emptyCustomer));

        assertThatExceptionOfType(EmptyCartException.class)
                .isThrownBy(() -> orderProcessor.processOrder(2L));

        verify(orderRepository, never()).save(any());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenStockIsInsufficient() {
        Customer greedyCustomer = new Customer(3L, "Piotr", "wiktor@gmail.com", "Password!123");
        greedyCustomer.getCart().addProduct(product, 15); // only 10 in stock
        when(customerRepository.findById(3L)).thenReturn(Optional.of(greedyCustomer));

        assertThatExceptionOfType(InsufficientStockException.class)
                .isThrownBy(() -> orderProcessor.processOrder(3L))
                .withMessageContaining("Monitor");
    }

    @Test
    void shouldGetInvoiceByOrderId() {
        List<Invoice> invoices = List.of(
                new Invoice(1L, 5L, 1L, "Jan", List.of(), new BigDecimal("1600"))
        );
        when(invoiceRepository.getAll()).thenReturn(invoices);

        InvoiceDto result = orderProcessor.getInvoiceByOrderId(5L);

        assertThat(result.orderId()).isEqualTo(5L);
    }

    @Test
    void shouldThrowWhenInvoiceForOrderNotFound() {
        when(invoiceRepository.getAll()).thenReturn(List.of());

        assertThatExceptionOfType(OrderNotFoundException.class)
                .isThrownBy(() -> orderProcessor.getInvoiceByOrderId(99L));
    }

    @Test
    void shouldGetAllInvoices() {
        List<Invoice> invoices = List.of(
                new Invoice(1L, 1L, 1L, "Jan", List.of(), new BigDecimal("1000")),
                new Invoice(2L, 2L, 2L, "Anna", List.of(), new BigDecimal("2000"))
        );
        when(invoiceRepository.getAll()).thenReturn(invoices);

        List<InvoiceDto> result = orderProcessor.getAllInvoices();

        assertThat(result).hasSize(2);
    }
}