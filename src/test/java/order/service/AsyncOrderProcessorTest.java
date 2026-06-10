package order.service;

import customer.model.Customer;
import customer.repository.CustomerRepository;
import discount.service.DiscountService;
import exception.EmptyCartException;
import invoice.dto.InvoiceDto;
import invoice.repository.InvoiceRepository;
import order.model.OrderProcessingResult;
import order.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.model.electronics.Electronics;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsyncOrderProcessorTest {

    @Mock OrderRepository orderRepository;
    @Mock CustomerRepository customerRepository;
    @Mock InvoiceRepository invoiceRepository;
    @Mock DiscountService discountService;

    private OrderProcessor orderProcessor;
    private AsyncOrderProcessor asyncProcessor;

    @BeforeEach
    void setUp() {
        orderProcessor = new OrderProcessor(orderRepository, customerRepository,
                invoiceRepository, discountService);
        asyncProcessor = new AsyncOrderProcessor(orderProcessor, 2);
    }

    @AfterEach
    void tearDown() {
        asyncProcessor.shutdown();
    }

    private Customer customerWithProduct(Long id, String name, int stock) {
        Electronics product = new Electronics(id, "Monitor " + id, new BigDecimal("800"), stock);
        Customer customer = new Customer(id, name);
        customer.getCart().addProduct(product, 1);
        return customer;
    }

    @Test
    void shouldReturnCompletableFutureForSuccessfulOrder() throws Exception {
        Customer customer = customerWithProduct(1L, "Jan", 5);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.getNextId()).thenReturn(1L);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.getNextId()).thenReturn(1L);
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CompletableFuture<InvoiceDto> future = asyncProcessor.processOrderAsync(1L);
        InvoiceDto invoice = future.get();

        assertThat(invoice).isNotNull();
        assertThat(invoice.customerId()).isEqualTo(1L);
    }

    @Test
    void shouldCompleteExceptionallyForEmptyCart() {
        Customer empty = new Customer(2L, "Anna");
        when(customerRepository.findById(2L)).thenReturn(Optional.of(empty));

        CompletableFuture<InvoiceDto> future = asyncProcessor.processOrderAsync(2L);

        assertThatThrownBy(future::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(EmptyCartException.class);
    }

    @Test
    void shouldNotBlockCallingThread() throws Exception {
        Customer customer = customerWithProduct(1L, "Jan", 5);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(orderRepository.getNextId()).thenReturn(1L);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.getNextId()).thenReturn(1L);
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        long start = System.currentTimeMillis();
        CompletableFuture<InvoiceDto> future = asyncProcessor.processOrderAsync(1L);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isLessThan(1000);

        assertThat(future.get().customerId()).isEqualTo(1L);
    }

    @Test
    void shouldProcessMultipleOrdersAsynchronously() throws Exception {
        for (long id = 1; id <= 3; id++) {
            Customer c = customerWithProduct(id, "Customer " + id, 10);
            when(customerRepository.findById(id)).thenReturn(Optional.of(c));
            when(orderRepository.getNextId()).thenReturn(id);
            when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(invoiceRepository.getNextId()).thenReturn(id);
            when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        }

        List<OrderProcessingResult> results = asyncProcessor
                .processBatchAsync(List.of(1L, 2L, 3L))
                .get();

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(OrderProcessingResult::success);
    }

    @Test
    void shouldHandleMixedSuccessAndFailureInBatch() throws Exception {
        Customer ok = customerWithProduct(1L, "Jan", 5);
        Customer fail = new Customer(2L, "Anna"); // pusty koszyk
        when(customerRepository.findById(1L)).thenReturn(Optional.of(ok));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(fail));
        when(orderRepository.getNextId()).thenReturn(1L);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.getNextId()).thenReturn(1L);
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<OrderProcessingResult> results = asyncProcessor
                .processBatchAsync(List.of(1L, 2L))
                .get();

        assertThat(results).hasSize(2);
        assertThat(results.stream().filter(OrderProcessingResult::success)).hasSize(1);
        assertThat(results.stream().filter(r -> !r.success())).hasSize(1);
    }

    @Test
    void shouldReturnResultsInInputOrder() throws Exception {
        Customer c1 = customerWithProduct(1L, "Jan", 5);
        Customer c2 = customerWithProduct(2L, "Anna", 5);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(c1));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(c2));
        when(orderRepository.getNextId()).thenReturn(1L).thenReturn(2L);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(invoiceRepository.getNextId()).thenReturn(1L).thenReturn(2L);
        when(invoiceRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        List<OrderProcessingResult> results = asyncProcessor
                .processBatchAsync(List.of(1L, 2L))
                .get();

        assertThat(results.get(0).customerId()).isEqualTo(1L);
        assertThat(results.get(1).customerId()).isEqualTo(2L);
    }

    @Test
    void shouldHandleEmptyBatch() throws Exception {
        List<OrderProcessingResult> results = asyncProcessor
                .processBatchAsync(List.of())
                .get();

        assertThat(results).isEmpty();
    }
}

