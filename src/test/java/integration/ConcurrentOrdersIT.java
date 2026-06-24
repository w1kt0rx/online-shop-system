package integration;

import cart.service.CartService;
import customer.dto.CreateCustomerRequest;
import customer.dto.CustomerDto;
import customer.repository.impl.InMemoryCustomerRepository;
import customer.service.CustomerService;
import discount.repository.impl.InMemoryDiscountRepository;
import discount.service.DiscountService;
import invoice.dto.InvoiceDto;
import invoice.repository.impl.InMemoryInvoiceRepository;
import invoice.service.InvoiceService;
import order.facade.OrderFacade;
import order.model.OrderProcessingResult;
import order.repository.impl.InMemoryOrderRepository;
import order.service.AsyncOrderProcessor;
import order.service.ConcurrentOrderProcessor;
import order.service.OrderProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.dto.electronics.CreateElectronicsRequest;
import product.dto.electronics.ElectronicsDto;
import product.model.ProductType;
import product.repository.impl.*;
import product.service.ElectronicsService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for concurrent and async order processing — thread-safety,
 * no overselling, result ordering, and mixed success/failure handling.
 */
class ConcurrentOrdersIT {

    private CustomerService customerService;
    private ElectronicsService electronicsService;
    private CartService cartService;
    private OrderProcessor orderProcessor;
    private OrderFacade orderFacade;
    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        InMemoryCustomerRepository customerRepo   = new InMemoryCustomerRepository();
        InMemoryComputerRepository computerRepo   = new InMemoryComputerRepository();
        InMemorySmartphoneRepository phoneRepo    = new InMemorySmartphoneRepository();
        InMemoryElectronicsRepository elRepo      = new InMemoryElectronicsRepository();
        InMemoryOrderRepository orderRepo         = new InMemoryOrderRepository();
        InMemoryInvoiceRepository invoiceRepo     = new InMemoryInvoiceRepository();

        customerService    = new CustomerService(customerRepo);
        electronicsService = new ElectronicsService(elRepo);
        invoiceService     = new InvoiceService(invoiceRepo);
        cartService        = new CartService(customerRepo, computerRepo, phoneRepo, elRepo);
        orderProcessor     = new OrderProcessor(orderRepo, customerRepo,
                new DiscountService(new InMemoryDiscountRepository()), invoiceService);

        ConcurrentOrderProcessor concurrent = new ConcurrentOrderProcessor(orderProcessor, 4);
        AsyncOrderProcessor async           = new AsyncOrderProcessor(orderProcessor, 4);
        orderFacade = new OrderFacade(concurrent, async);
    }

    @AfterEach
    void tearDown() {
        orderFacade.shutdown();
    }

    private CustomerDto newCustomer(int n) {
        return customerService.createCustomer(
                new CreateCustomerRequest("Customer " + n, "c" + n + "@example.com", "Password123"));
    }

    private List<Long> prepareCustomers(int count, Long productId, int qty) {
        AtomicInteger seq = new AtomicInteger(1);
        return IntStream.range(0, count)
                .mapToObj(i -> {
                    CustomerDto c = newCustomer(seq.getAndIncrement());
                    cartService.addProduct(c.id(), productId, ProductType.ELECTRONICS, qty);
                    return c.id();
                })
                .toList();
    }


    @Test
    void concurrentBatch_allSucceed_stockExactlyZero() {
        final int UNITS = 6;
        ElectronicsDto product = electronicsService.create(
                new CreateElectronicsRequest("USB Hub", new BigDecimal("150"), UNITS));

        List<Long> customerIds = prepareCustomers(UNITS, product.id(), 1);

        List<OrderProcessingResult> results = orderFacade.processBatchOrders(customerIds);

        assertThat(results).hasSize(UNITS);
        assertThat(results).allSatisfy(r -> {
            assertThat(r.success()).isTrue();
            assertThat(r.invoice()).isNotNull();
            assertThat(r.invoice().totalAmount()).isEqualByComparingTo(new BigDecimal("150"));
            assertThat(r.errorMessage()).isNull();
        });
        assertThat(electronicsService.getById(product.id()).quantity()).isEqualTo(0);
        assertThat(invoiceService.getAllInvoices()).hasSize(UNITS);
    }

    @Test
    void concurrentBatch_stockLimitedToFive_exactlyFiveSucceed_restFail() {
        final int STOCK = 5;
        final int CUSTOMERS = 8;
        ElectronicsDto product = electronicsService.create(
                new CreateElectronicsRequest("Limited Item", new BigDecimal("200"), STOCK));

        List<Long> customerIds = prepareCustomers(CUSTOMERS, product.id(), 1);

        List<OrderProcessingResult> results = orderFacade.processBatchOrders(customerIds);

        assertThat(results).hasSize(CUSTOMERS);

        long successes = results.stream().filter(OrderProcessingResult::success).count();
        long failures  = results.stream().filter(r -> !r.success()).count();

        assertThat(successes).isEqualTo(STOCK);
        assertThat(failures).isEqualTo(CUSTOMERS - STOCK);

        assertThat(electronicsService.getById(product.id()).quantity()).isEqualTo(0);
    }

    @Test
    void concurrentBatch_resultsReturnedInInputOrder() {
        final int COUNT = 4;
        ElectronicsDto product = electronicsService.create(
                new CreateElectronicsRequest("Ordered Item", new BigDecimal("100"), COUNT));

        List<CustomerDto> customers = new ArrayList<>();
        for (int i = 1; i <= COUNT; i++) {
            CustomerDto c = newCustomer(i + 100);
            cartService.addProduct(c.id(), product.id(), ProductType.ELECTRONICS, 1);
            customers.add(c);
        }
        List<Long> ids = customers.stream().map(CustomerDto::id).toList();

        List<OrderProcessingResult> results = orderFacade.processBatchOrders(ids);

        assertThat(results).hasSize(COUNT);
        for (int i = 0; i < COUNT; i++) {
            assertThat(results.get(i).customerId()).isEqualTo(ids.get(i));
        }
    }

    @Test
    void concurrentBatch_emptyInputList_returnsEmptyResults() {
        List<OrderProcessingResult> results = orderFacade.processBatchOrders(List.of());

        assertThat(results).isEmpty();
    }

    @Test
    void concurrentBatch_customerWithEmptyCart_recordedAsFailure_othersUnaffected() {
        ElectronicsDto product = electronicsService.create(
                new CreateElectronicsRequest("Item", new BigDecimal("300"), 5));

        CustomerDto withCart    = newCustomer(200);
        CustomerDto withoutCart = newCustomer(201);
        cartService.addProduct(withCart.id(), product.id(), ProductType.ELECTRONICS, 1);

        List<OrderProcessingResult> results =
                orderFacade.processBatchOrders(List.of(withCart.id(), withoutCart.id()));

        assertThat(results).hasSize(2);
        OrderProcessingResult ok   = results.stream().filter(OrderProcessingResult::success).findFirst().orElseThrow();
        OrderProcessingResult fail = results.stream().filter(r -> !r.success()).findFirst().orElseThrow();

        assertThat(ok.customerId()).isEqualTo(withCart.id());
        assertThat(ok.invoice()).isNotNull();
        assertThat(fail.customerId()).isEqualTo(withoutCart.id());
        assertThat(fail.errorMessage()).isNotBlank();
    }


    @Test
    void asyncSingle_completesWithCorrectInvoice() throws Exception {
        ElectronicsDto product = electronicsService.create(
                new CreateElectronicsRequest("SSD 1TB", new BigDecimal("400"), 5));
        CustomerDto customer = newCustomer(300);
        cartService.addProduct(customer.id(), product.id(), ProductType.ELECTRONICS, 2);

        CompletableFuture<InvoiceDto> future =
                orderFacade.processOrderAsync(customer.id());
        InvoiceDto invoice = future.get();

        assertThat(invoice).isNotNull();
        assertThat(invoice.customerId()).isEqualTo(customer.id());
        assertThat(invoice.totalAmount()).isEqualByComparingTo(new BigDecimal("800"));
        assertThat(invoice.issuedAt()).isNotNull();
        assertThat(cartService.getCart(customer.id()).items()).isEmpty();
        assertThat(electronicsService.getById(product.id()).quantity()).isEqualTo(3);
    }

    @Test
    void asyncBatch_allSucceed_noOverselling() throws Exception {
        final int UNITS = 4;
        ElectronicsDto product = electronicsService.create(
                new CreateElectronicsRequest("RAM 32GB", new BigDecimal("250"), UNITS));

        List<Long> ids = prepareCustomers(UNITS, product.id(), 1);

        List<OrderProcessingResult> results =
                orderFacade.processBatchAsync(ids).get();

        assertThat(results).hasSize(UNITS);
        assertThat(results).allSatisfy(r -> assertThat(r.success()).isTrue());
        assertThat(electronicsService.getById(product.id()).quantity()).isEqualTo(0);
    }

    @Test
    void asyncBatch_mixedResults_failuresCarryErrorMessages() throws Exception {
        ElectronicsDto product = electronicsService.create(
                new CreateElectronicsRequest("Last Unit", new BigDecimal("500"), 1));

        CustomerDto buyer1 = newCustomer(400);
        CustomerDto buyer2 = newCustomer(401); // empty cart
        CustomerDto buyer3 = newCustomer(402);
        cartService.addProduct(buyer1.id(), product.id(), ProductType.ELECTRONICS, 1);
        cartService.addProduct(buyer3.id(), product.id(), ProductType.ELECTRONICS, 1);

        List<OrderProcessingResult> results =
                orderFacade.processBatchAsync(
                        List.of(buyer1.id(), buyer2.id(), buyer3.id())).get();

        assertThat(results).hasSize(3);
        long successes = results.stream().filter(OrderProcessingResult::success).count();

        assertThat(successes).isEqualTo(1);
        assertThat(results.stream()
                .filter(r -> !r.success())
                .allMatch(r -> r.errorMessage() != null && !r.errorMessage().isBlank()))
                .isTrue();
        assertThat(electronicsService.getById(product.id()).quantity()).isEqualTo(0);
    }
}