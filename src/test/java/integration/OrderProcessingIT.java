package integration;

import static org.assertj.core.api.Assertions.*;

import cart.service.CartService;
import customer.dto.CreateCustomerRequest;
import customer.dto.CustomerDto;
import customer.repository.impl.InMemoryCustomerRepository;
import customer.service.CustomerService;
import discount.dto.CreateDiscountRequest;
import discount.model.DiscountType;
import discount.repository.impl.InMemoryDiscountRepository;
import discount.service.DiscountService;
import exception.*;
import invoice.dto.InvoiceDto;
import invoice.repository.impl.InMemoryInvoiceRepository;
import invoice.service.InvoiceService;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import order.dto.OrderDto;
import order.model.OrderStatus;
import order.repository.impl.InMemoryOrderRepository;
import order.service.OrderProcessor;
import order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.dto.electronics.CreateElectronicsRequest;
import product.dto.electronics.ElectronicsDto;
import product.model.ProductType;
import product.repository.impl.*;
import product.service.*;

/**
 * Integration tests for the order placement pipeline — happy paths,
 * discount application, empty cart guard, stock validation, roll-back on
 * partial failure, and order history queries.
 */
class OrderProcessingIT {

    private CustomerService customerService;
    private ElectronicsService electronicsService;
    private CartService cartService;
    private OrderProcessor orderProcessor;
    private OrderService orderService;
    private DiscountService discountService;
    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        InMemoryCustomerRepository customerRepo = new InMemoryCustomerRepository();
        InMemoryComputerRepository computerRepo = new InMemoryComputerRepository();
        InMemorySmartphoneRepository phoneRepo = new InMemorySmartphoneRepository();
        InMemoryElectronicsRepository elRepo = new InMemoryElectronicsRepository();
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        InMemoryInvoiceRepository invoiceRepo = new InMemoryInvoiceRepository();
        InMemoryDiscountRepository discountRepo = new InMemoryDiscountRepository();

        customerService = new CustomerService(customerRepo);
        electronicsService = new ElectronicsService(elRepo);
        discountService = new DiscountService(discountRepo);
        invoiceService = new InvoiceService(invoiceRepo);
        cartService = new CartService(customerRepo, computerRepo, phoneRepo, elRepo);
        orderService = new OrderService(orderRepo, customerRepo);
        orderProcessor = new OrderProcessor(orderRepo, customerRepo, discountService, invoiceService);
    }

    private CustomerDto newCustomer(String name, String email) {
        return customerService.createCustomer(new CreateCustomerRequest(name, email, "Password123"));
    }

    private ElectronicsDto newProduct(String name, BigDecimal price, int stock) {
        return electronicsService.create(new CreateElectronicsRequest(name, price, stock));
    }

    @Test
    void processOrder_singleItem_invoiceIssuedAndCartCleared() {
        ElectronicsDto product = newProduct("Monitor", new BigDecimal("1200"), 5);
        CustomerDto customer = newCustomer("Jan", "jan@example.com");

        cartService.addProduct(customer.id(), product.id(), ProductType.ELECTRONICS, 2);
        InvoiceDto invoice = orderProcessor.processOrder(customer.id());

        assertThat(invoice.id()).isPositive();
        assertThat(invoice.orderId()).isPositive();
        assertThat(invoice.customerId()).isEqualTo(customer.id());
        assertThat(invoice.customerName()).isEqualTo("Jan");
        assertThat(invoice.totalAmount()).isEqualByComparingTo(new BigDecimal("2400"));
        assertThat(invoice.items()).hasSize(1);
        assertThat(invoice.items().get(0).productName()).isEqualTo("Monitor");
        assertThat(invoice.items().get(0).quantity()).isEqualTo(2);
        assertThat(invoice.issuedAt()).isNotNull();

        assertThat(cartService.getCart(customer.id()).items()).isEmpty();
        assertThat(electronicsService.getById(product.id()).quantity()).isEqualTo(3);
    }

    @Test
    void processOrder_multipleItems_totalSumsAllLines() {
        ElectronicsDto a = newProduct("Keyboard", new BigDecimal("300"), 10);
        ElectronicsDto b = newProduct("Mouse", new BigDecimal("150"), 10);
        ElectronicsDto c = newProduct("Webcam", new BigDecimal("250"), 10);
        CustomerDto customer = newCustomer("Anna", "anna@example.com");

        cartService.addProduct(customer.id(), a.id(), ProductType.ELECTRONICS, 2);
        cartService.addProduct(customer.id(), b.id(), ProductType.ELECTRONICS, 1);
        cartService.addProduct(customer.id(), c.id(), ProductType.ELECTRONICS, 3);
        InvoiceDto invoice = orderProcessor.processOrder(customer.id());

        // 2×300 + 1×150 + 3×250 = 1500
        assertThat(invoice.totalAmount()).isEqualByComparingTo(new BigDecimal("1500"));
        assertThat(invoice.items()).hasSize(3);
    }

    @Test
    void processOrder_orderAppearsInHistoryWithConfirmedStatus() {
        ElectronicsDto product = newProduct("SSD", new BigDecimal("400"), 5);
        CustomerDto customer = newCustomer("Piotr", "piotr@example.com");

        cartService.addProduct(customer.id(), product.id(), ProductType.ELECTRONICS, 1);
        InvoiceDto invoice = orderProcessor.processOrder(customer.id());

        List<OrderDto> history = orderService.getOrdersByCustomer(customer.id());
        assertThat(history).hasSize(1);

        OrderDto order = history.get(0);
        assertThat(order.id()).isEqualTo(invoice.orderId());
        assertThat(order.customerId()).isEqualTo(customer.id());
        assertThat(order.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(order.totalPrice()).isEqualByComparingTo(new BigDecimal("400"));
        assertThat(order.createdAt()).isNotNull();
        assertThat(order.confirmedAt()).isNotNull();
        assertThat(order.updatedAt()).isNotNull();
    }

    @Test
    void processOrder_multipleOrdersSameCustomer_allInHistory() {
        ElectronicsDto product = newProduct("USB Hub", new BigDecimal("100"), 10);
        CustomerDto customer = newCustomer("Ewa", "ewa@example.com");

        cartService.addProduct(customer.id(), product.id(), ProductType.ELECTRONICS, 1);
        orderProcessor.processOrder(customer.id());

        cartService.addProduct(customer.id(), product.id(), ProductType.ELECTRONICS, 2);
        orderProcessor.processOrder(customer.id());

        cartService.addProduct(customer.id(), product.id(), ProductType.ELECTRONICS, 1);
        orderProcessor.processOrder(customer.id());

        List<OrderDto> history = orderService.getOrdersByCustomer(customer.id());
        assertThat(history).hasSize(3);
        assertThat(history).allSatisfy(o -> assertThat(o.orderStatus()).isEqualTo(OrderStatus.CONFIRMED));
        // stock: 10 − 1 − 2 − 1 = 6
        assertThat(electronicsService.getById(product.id()).quantity()).isEqualTo(6);
    }

    @Test
    void processOrder_withPercentageDiscount_totalReducedCorrectly() {
        ZonedDateTime now = ZonedDateTime.now();
        discountService.createDiscount(
            CreateDiscountRequest.of(
                "SAVE10",
                "10% off",
                DiscountType.PERCENTAGE,
                new BigDecimal("10"),
                BigDecimal.ZERO,
                now.minusDays(1),
                now.plusDays(30)
            )
        );

        ElectronicsDto product = newProduct("Laptop", new BigDecimal("3000"), 2);
        CustomerDto customer = newCustomer("Maria", "maria@example.com");

        cartService.addProduct(customer.id(), product.id(), ProductType.ELECTRONICS, 1);
        InvoiceDto invoice = orderProcessor.processOrder(customer.id(), "SAVE10");

        // 3000 − 10% = 2700
        assertThat(invoice.totalAmount()).isEqualByComparingTo(new BigDecimal("2700.00"));
        assertThat(invoice.items()).hasSize(1);
        assertThat(invoice.customerId()).isEqualTo(customer.id());
    }

    @Test
    void processOrder_withFixedAmountDiscount_totalReducedCorrectly() {
        ZonedDateTime now = ZonedDateTime.now();
        discountService.createDiscount(
            CreateDiscountRequest.of(
                "MINUS500",
                "500 PLN off",
                DiscountType.FIXED_AMOUNT,
                new BigDecimal("500"),
                new BigDecimal("2000"),
                now.minusDays(1),
                now.plusDays(30)
            )
        );

        ElectronicsDto product = newProduct("TV", new BigDecimal("3500"), 2);
        CustomerDto customer = newCustomer("Karol", "karol@example.com");

        cartService.addProduct(customer.id(), product.id(), ProductType.ELECTRONICS, 1);
        InvoiceDto invoice = orderProcessor.processOrder(customer.id(), "MINUS500");

        assertThat(invoice.totalAmount()).isEqualByComparingTo(new BigDecimal("3000"));
    }

    @Test
    void processOrder_expiredDiscountCode_orderSucceedsAtFullPrice() {
        ZonedDateTime now = ZonedDateTime.now();
        discountService.createDiscount(
            CreateDiscountRequest.of(
                "EXPIRED",
                "Old deal",
                DiscountType.PERCENTAGE,
                new BigDecimal("20"),
                BigDecimal.ZERO,
                now.minusDays(30),
                now.minusDays(1)
            )
        ); // already expired

        ElectronicsDto product = newProduct("Monitor", new BigDecimal("1000"), 3);
        CustomerDto customer = newCustomer("Test", "test@example.com");

        cartService.addProduct(customer.id(), product.id(), ProductType.ELECTRONICS, 1);
        // Expired code → order still goes through at full price (graceful fallback)
        InvoiceDto invoice = orderProcessor.processOrder(customer.id(), "EXPIRED");

        assertThat(invoice.totalAmount()).isEqualByComparingTo(new BigDecimal("1000"));
    }

    @Test
    void processOrder_emptyCart_throwsEmptyCartException_noInvoiceCreated() {
        CustomerDto customer = newCustomer("Empty", "empty@example.com");

        assertThatExceptionOfType(EmptyCartException.class).isThrownBy(() -> orderProcessor.processOrder(customer.id())
        );

        assertThat(invoiceService.getAllInvoices()).isEmpty();
        assertThat(orderService.getAllOrders()).isEmpty();
    }

    @Test
    void processOrder_insufficientStock_throwsAndNoInvoiceCreated() {
        ElectronicsDto product = newProduct("Rare Item", new BigDecimal("500"), 1);
        CustomerDto c1 = newCustomer("First", "first@example.com");
        CustomerDto c2 = newCustomer("Second", "second@example.com");

        cartService.addProduct(c1.id(), product.id(), ProductType.ELECTRONICS, 1);
        cartService.addProduct(c2.id(), product.id(), ProductType.ELECTRONICS, 1);

        orderProcessor.processOrder(c1.id());

        assertThatExceptionOfType(InsufficientStockException.class)
            .isThrownBy(() -> orderProcessor.processOrder(c2.id()))
            .withMessageContaining("Rare Item");

        assertThat(invoiceService.getAllInvoices()).hasSize(1);
        assertThat(invoiceService.getAllInvoices().get(0).customerId()).isEqualTo(c1.id());
    }

    @Test
    void processOrder_unknownCustomer_throwsCustomerNotFoundException() {
        assertThatExceptionOfType(CustomerNotFoundException.class).isThrownBy(() -> orderProcessor.processOrder(999L));
    }

    @Test
    void processOrder_partialStockFailure_allStockRolledBack() {
        ElectronicsDto goodItem = newProduct("Keyboard", new BigDecimal("200"), 5);
        ElectronicsDto lastItem = newProduct("Last GPU", new BigDecimal("1000"), 1);

        CustomerDto buyer = newCustomer("Buyer", "buyer@example.com");
        CustomerDto sniper = newCustomer("Sniper", "sniper@example.com");

        cartService.addProduct(buyer.id(), goodItem.id(), ProductType.ELECTRONICS, 2);
        cartService.addProduct(buyer.id(), lastItem.id(), ProductType.ELECTRONICS, 1);
        cartService.addProduct(sniper.id(), lastItem.id(), ProductType.ELECTRONICS, 1);

        orderProcessor.processOrder(sniper.id());
        assertThat(electronicsService.getById(lastItem.id()).quantity()).isEqualTo(0);

        assertThatExceptionOfType(InsufficientStockException.class).isThrownBy(() ->
            orderProcessor.processOrder(buyer.id())
        );

        assertThat(electronicsService.getById(goodItem.id()).quantity()).isEqualTo(5);
        assertThat(invoiceService.getAllInvoices()).hasSize(1);
    }
}
