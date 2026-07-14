package order.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

import cart.dto.CartItemDto;
import common.time.TimeUtils;
import customer.model.Customer;
import customer.repository.CustomerRepository;
import discount.service.DiscountService;
import exception.EmptyCartException;
import invoice.dto.InvoiceDto;
import invoice.service.InvoiceService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import order.model.Order;
import order.model.OrderProcessingResult;
import order.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.model.electronics.Electronics;

@ExtendWith(MockitoExtension.class)
class AsyncOrderProcessorTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private DiscountService discountService;

    @Mock
    private InvoiceService invoiceService;

    private OrderProcessor orderProcessor;
    private AsyncOrderProcessor asyncProcessor;

    @BeforeEach
    void setUp() {
        orderProcessor = new OrderProcessor(orderRepository, customerRepository, discountService, invoiceService);
        asyncProcessor = new AsyncOrderProcessor(orderProcessor, 2);
    }

    @AfterEach
    void tearDown() {
        asyncProcessor.shutdown();
    }

    private Customer customerWithProduct(Long id, String name, int stock) {
        Electronics product = new Electronics(id, "Monitor " + id, new BigDecimal("800"), stock);
        Customer customer = new Customer(id, name, "user" + id + "@example.com", "Password123");
        customer.getCart().addProduct(product, 1);
        return customer;
    }

    private InvoiceDto buildInvoiceDto(
        Long invoiceId,
        Long orderId,
        Long customerId,
        String customerName,
        BigDecimal totalPrice
    ) {
        return new InvoiceDto(invoiceId, orderId, customerId, customerName, List.of(), totalPrice, TimeUtils.now());
    }

    private void stubOrderSaveAssigningIds() {
        AtomicLong sequence = new AtomicLong(1L);
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            if (order.getId() == null) {
                order.setId(sequence.getAndIncrement());
            }
            return order;
        });
    }

    private void stubInvoiceCreation() {
        when(invoiceService.createInvoice(any(), anyLong(), anyString(), anyList(), any())).thenAnswer(invocation -> {
            Long orderId = invocation.getArgument(0);
            Long customerId = invocation.getArgument(1);
            String name = invocation.getArgument(2);
            BigDecimal amount = invocation.getArgument(4);
            return buildInvoiceDto(1000L + customerId, orderId, customerId, name, amount);
        });
    }

    @Test
    void shouldReturnCompletableFutureForSuccessfulOrder() throws Exception {
        Customer customer = customerWithProduct(1L, "Jan", 5);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        stubOrderSaveAssigningIds();
        stubInvoiceCreation();

        CompletableFuture<InvoiceDto> future = asyncProcessor.processOrderAsync(1L);
        InvoiceDto invoice = future.get();

        assertThat(invoice).isNotNull();
        assertThat(invoice.customerId()).isEqualTo(1L);
        assertThat(invoice.customerName()).isEqualTo("Jan");
        assertThat(invoice.orderId()).isNotNull();
    }

    @Test
    void shouldCompleteExceptionallyForEmptyCart() {
        Customer empty = new Customer(2L, "Anna", "anna@example.com", "Password123");
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
        stubOrderSaveAssigningIds();
        stubInvoiceCreation();

        long start = System.currentTimeMillis();
        CompletableFuture<InvoiceDto> future = asyncProcessor.processOrderAsync(1L);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(elapsed).isLessThan(1000);

        InvoiceDto invoice = future.get();
        assertThat(invoice).isNotNull();
        assertThat(invoice.customerId()).isEqualTo(1L);
    }

    @Test
    void shouldProcessMultipleOrdersAsynchronously() throws Exception {
        for (long id = 1; id <= 3; id++) {
            Customer customer = customerWithProduct(id, "Customer " + id, 10);
            when(customerRepository.findById(id)).thenReturn(Optional.of(customer));
        }

        stubOrderSaveAssigningIds();
        stubInvoiceCreation();

        List<OrderProcessingResult> results = asyncProcessor.processBatchAsync(List.of(1L, 2L, 3L)).get();

        assertThat(results).hasSize(3);
        assertThat(results).allMatch(OrderProcessingResult::success);
        assertThat(results).extracting(OrderProcessingResult::customerId).containsExactly(1L, 2L, 3L);
    }

    @Test
    void shouldHandleMixedSuccessAndFailureInBatch() throws Exception {
        Customer ok = customerWithProduct(1L, "Jan", 5);
        Customer fail = new Customer(2L, "Anna", "anna@example.com", "Password123");

        when(customerRepository.findById(1L)).thenReturn(Optional.of(ok));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(fail));

        stubOrderSaveAssigningIds();
        stubInvoiceCreation();

        List<OrderProcessingResult> results = asyncProcessor.processBatchAsync(List.of(1L, 2L)).get();

        assertThat(results).hasSize(2);
        assertThat(results.stream().filter(OrderProcessingResult::success)).hasSize(1);
        assertThat(results.stream().filter(r -> !r.success())).hasSize(1);

        OrderProcessingResult success = results
            .stream()
            .filter(OrderProcessingResult::success)
            .findFirst()
            .orElseThrow();

        OrderProcessingResult failure = results.stream().filter(r -> !r.success()).findFirst().orElseThrow();

        assertThat(success.customerId()).isEqualTo(1L);
        assertThat(failure.customerId()).isEqualTo(2L);
        assertThat(failure.errorMessage()).containsIgnoringCase("empty");
    }

    @Test
    void shouldReturnResultsInInputOrder() throws Exception {
        Customer c1 = customerWithProduct(1L, "Jan", 5);
        Customer c2 = customerWithProduct(2L, "Anna", 5);

        when(customerRepository.findById(1L)).thenReturn(Optional.of(c1));
        when(customerRepository.findById(2L)).thenReturn(Optional.of(c2));

        stubOrderSaveAssigningIds();
        stubInvoiceCreation();

        List<OrderProcessingResult> results = asyncProcessor.processBatchAsync(List.of(1L, 2L)).get();

        assertThat(results).hasSize(2);
        assertThat(results.get(0).customerId()).isEqualTo(1L);
        assertThat(results.get(1).customerId()).isEqualTo(2L);
        assertThat(results).allMatch(OrderProcessingResult::success);
    }

    @Test
    void shouldHandleEmptyBatch() throws Exception {
        List<OrderProcessingResult> results = asyncProcessor.processBatchAsync(List.of()).get();

        assertThat(results).isEmpty();
    }
}
