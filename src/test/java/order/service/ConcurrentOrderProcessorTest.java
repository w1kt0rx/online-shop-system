package order.service;

import invoice.dto.InvoiceDto;
import invoice.model.Invoice;
import order.model.OrderProcessingResult;
import order.model.OrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.model.electronics.Electronics;
import customer.model.Customer;
import customer.repository.CustomerRepository;
import order.repository.OrderRepository;
import invoice.repository.InvoiceRepository;
import discount.service.DiscountService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConcurrentOrderProcessorTest {

    @Mock
    OrderRepository orderRepository;
    @Mock
    CustomerRepository customerRepository;
    @Mock
    InvoiceRepository invoiceRepository;
    @Mock
    DiscountService discountService;

    @Test
    void shouldProcessMultipleOrdersConcurrently() {
        Electronics product1 = new Electronics(1L, "Monitor", new BigDecimal("800"), 10);
        Electronics product2 = new Electronics(2L, "Keyboard", new BigDecimal("300"), 10);

        Customer c1 = new Customer(1L, "Jan");
        Customer c2 = new Customer(2L, "Anna");
        c1.getCart().addProduct(product1, 1);
        c2.getCart().addProduct(product2, 1);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(c1));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(c2));
        when(orderRepository.getNextId()).thenReturn(1L).thenReturn(2L);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.getNextId()).thenReturn(1L).thenReturn(2L);
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderProcessor processor = new OrderProcessor(
                orderRepository, customerRepository, invoiceRepository, discountService);
        ConcurrentOrderProcessor concurrent = new ConcurrentOrderProcessor(processor, 2);

        List<OrderProcessingResult> results = concurrent.processOrdersConcurrently(List.of(1L, 2L));

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(OrderProcessingResult::success));
    }

    @Test
    void shouldReturnFailureForCustomerWithEmptyCart() {
        Customer emptyCustomer = new Customer(3L, "Piotr");
        when(customerRepository.findById(3L)).thenReturn(Optional.of(emptyCustomer));

        OrderProcessor processor = new OrderProcessor(
                orderRepository, customerRepository, invoiceRepository, discountService);
        ConcurrentOrderProcessor concurrent = new ConcurrentOrderProcessor(processor, 2);

        List<OrderProcessingResult> results = concurrent.processOrdersConcurrently(List.of(3L));

        assertEquals(1, results.size());
        assertFalse(results.get(0).success());
        assertNotNull(results.get(0).errorMessage());
    }

    @Test
    void shouldHandleMixOfSuccessAndFailure() {
        Electronics product = new Electronics(1L, "Monitor", new BigDecimal("800"), 10);
        Customer success = new Customer(1L, "Jan");
        success.getCart().addProduct(product, 1);
        Customer failure = new Customer(2L, "Anna"); // empty cart

        when(customerRepository.findById(1L)).thenReturn(Optional.of(success));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(failure));
        when(orderRepository.getNextId()).thenReturn(1L);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.getNextId()).thenReturn(1L);
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderProcessor processor = new OrderProcessor(
                orderRepository, customerRepository, invoiceRepository, discountService);
        ConcurrentOrderProcessor concurrent = new ConcurrentOrderProcessor(processor, 2);

        List<OrderProcessingResult> results = concurrent.processOrdersConcurrently(List.of(1L, 2L));

        assertEquals(2, results.size());
        long successCount = results.stream().filter(OrderProcessingResult::success).count();
        long failCount = results.stream().filter(r -> !r.success()).count();
        assertEquals(1, successCount);
        assertEquals(1, failCount);
    }
}