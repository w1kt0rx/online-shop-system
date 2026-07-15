package order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cart.dto.CartItemDto;
import customer.model.Customer;
import customer.repository.CustomerRepository;
import discount.service.DiscountService;
import invoice.dto.InvoiceDto;
import invoice.service.InvoiceService;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import order.model.OrderProcessingResult;
import order.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.model.electronics.Electronics;

@ExtendWith(MockitoExtension.class)
class ConcurrentOrderProcessorTest {

    @Mock
    OrderRepository orderRepository;

    @Mock
    CustomerRepository customerRepository;

    @Mock
    DiscountService discountService;

    @Mock
    InvoiceService invoiceService;

    private InvoiceDto buildInvoiceDto(Long customerId, String name, BigDecimal total) {
        return new InvoiceDto(1L, 1L, customerId, name, List.of(), total, ZonedDateTime.now());
    }

    @Test
    void shouldProcessMultipleOrdersConcurrently() {
        Electronics product1 = new Electronics(1L, "Monitor", new BigDecimal("800"), 10);
        Electronics product2 = new Electronics(2L, "Keyboard", new BigDecimal("300"), 10);

        Customer c1 = new Customer(1L, "Jan", "wiktor@gmail.com", "Password!123");
        Customer c2 = new Customer(2L, "Anna", "wiktor@gmail.com", "Password!123");
        c1.getCart().addProduct(product1, 1);
        c2.getCart().addProduct(product2, 1);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(c1));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(c2));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceService.createInvoice(any(), anyLong(), anyString(), anyList(), any())).thenAnswer(inv ->
            buildInvoiceDto(inv.getArgument(1), inv.getArgument(2), inv.getArgument(4))
        );

        OrderProcessor processor = new OrderProcessor(
            orderRepository,
            customerRepository,
            discountService,
            invoiceService
        );
        ConcurrentOrderProcessor concurrent = new ConcurrentOrderProcessor(processor, 2);

        List<OrderProcessingResult> results = concurrent.processOrdersConcurrently(List.of(1L, 2L));

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(OrderProcessingResult::success));
    }

    @Test
    void shouldReturnFailureForCustomerWithEmptyCart() {
        Customer emptyCustomer = new Customer(3L, "Piotr", "wiktor@gmail.com", "Password!123");
        when(customerRepository.findById(3L)).thenReturn(Optional.of(emptyCustomer));

        OrderProcessor processor = new OrderProcessor(
            orderRepository,
            customerRepository,
            discountService,
            invoiceService
        );
        ConcurrentOrderProcessor concurrent = new ConcurrentOrderProcessor(processor, 2);

        List<OrderProcessingResult> results = concurrent.processOrdersConcurrently(List.of(3L));

        assertEquals(1, results.size());
        assertFalse(results.get(0).success());
        assertNotNull(results.get(0).errorMessage());
    }

    @Test
    void shouldHandleMixOfSuccessAndFailure() {
        Electronics product = new Electronics(1L, "Monitor", new BigDecimal("800"), 10);
        Customer success = new Customer(1L, "Jan", "wiktor@gmail.com", "Password!123");
        success.getCart().addProduct(product, 1);
        Customer failure = new Customer(2L, "Anna", "wiktor@gmail.com", "Password!123"); // empty cart

        when(customerRepository.findById(1L)).thenReturn(Optional.of(success));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(failure));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceService.createInvoice(any(), anyLong(), anyString(), anyList(), any())).thenAnswer(inv ->
            buildInvoiceDto(inv.getArgument(1), inv.getArgument(2), inv.getArgument(4))
        );

        OrderProcessor processor = new OrderProcessor(
            orderRepository,
            customerRepository,
            discountService,
            invoiceService
        );
        ConcurrentOrderProcessor concurrent = new ConcurrentOrderProcessor(processor, 2);

        List<OrderProcessingResult> results = concurrent.processOrdersConcurrently(List.of(1L, 2L));

        assertEquals(2, results.size());
        long successCount = results.stream().filter(OrderProcessingResult::success).count();
        long failCount = results.stream().filter(r -> !r.success()).count();
        assertEquals(1, successCount);
        assertEquals(1, failCount);
    }
}
