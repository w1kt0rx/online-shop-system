package integration;

import static org.assertj.core.api.Assertions.*;

import cart.service.CartService;
import customer.dto.CreateCustomerRequest;
import customer.dto.CustomerDto;
import customer.repository.impl.InMemoryCustomerRepository;
import customer.service.CustomerService;
import discount.repository.impl.InMemoryDiscountRepository;
import discount.service.DiscountService;
import exception.OrderNotFoundException;
import invoice.dto.InvoiceDto;
import invoice.repository.impl.InMemoryInvoiceRepository;
import invoice.service.InvoiceService;
import java.math.BigDecimal;
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
import product.service.ElectronicsService;

class InvoiceAndOrderHistoryIT {

    private CustomerService customerService;
    private ElectronicsService electronicsService;
    private CartService cartService;
    private OrderProcessor orderProcessor;
    private OrderService orderService;
    private InvoiceService invoiceService;

    @BeforeEach
    void setUp() {
        InMemoryCustomerRepository customerRepo = new InMemoryCustomerRepository();
        InMemoryComputerRepository computerRepo = new InMemoryComputerRepository();
        InMemorySmartphoneRepository phoneRepo = new InMemorySmartphoneRepository();
        InMemoryElectronicsRepository elRepo = new InMemoryElectronicsRepository();
        InMemoryOrderRepository orderRepo = new InMemoryOrderRepository();
        InMemoryInvoiceRepository invoiceRepo = new InMemoryInvoiceRepository();

        customerService = new CustomerService(customerRepo);
        electronicsService = new ElectronicsService(elRepo);
        invoiceService = new InvoiceService(invoiceRepo);
        cartService = new CartService(customerRepo, computerRepo, phoneRepo, elRepo);
        orderService = new OrderService(orderRepo, customerRepo);
        orderProcessor = new OrderProcessor(
            orderRepo,
            customerRepo,
            new DiscountService(new InMemoryDiscountRepository()),
            invoiceService
        );
    }

    private CustomerDto newCustomer(String name, String email) {
        return customerService.createCustomer(new CreateCustomerRequest(name, email, "Password123"));
    }

    private ElectronicsDto newProduct(String name, BigDecimal price, int stock) {
        return electronicsService.create(new CreateElectronicsRequest(name, price, stock));
    }

    @Test
    void invoice_containsAllRequiredFields() {
        ElectronicsDto product = newProduct("Headphones", new BigDecimal("500"), 5);
        CustomerDto customer = newCustomer("Tomasz Bąk", "tomasz@example.com");

        cartService.addProduct(customer.id(), product.id(), ProductType.ELECTRONICS, 2);
        InvoiceDto invoice = orderProcessor.processOrder(customer.id());

        assertThat(invoice.id()).isPositive();
        assertThat(invoice.orderId()).isPositive();
        assertThat(invoice.customerId()).isEqualTo(customer.id());
        assertThat(invoice.customerName()).isEqualTo("Tomasz Bąk");
        assertThat(invoice.totalAmount()).isEqualByComparingTo(new BigDecimal("1000"));
        assertThat(invoice.issuedAt()).isNotNull();
        assertThat(invoice.issuedAt().getZone().getId()).isEqualTo("Europe/Warsaw");
        assertThat(invoice.items()).hasSize(1);
        assertThat(invoice.items().get(0).productName()).isEqualTo("Headphones");
        assertThat(invoice.items().get(0).quantity()).isEqualTo(2);
        assertThat(invoice.items().get(0).unitPrice()).isEqualByComparingTo(new BigDecimal("500"));
        assertThat(invoice.items().get(0).totalPrice()).isEqualByComparingTo(new BigDecimal("1000"));
    }

    @Test
    void invoice_retrievableByOrderId() {
        ElectronicsDto product = newProduct("Webcam", new BigDecimal("250"), 3);
        CustomerDto customer = newCustomer("Monika", "monika@example.com");

        cartService.addProduct(customer.id(), product.id(), ProductType.ELECTRONICS, 1);
        InvoiceDto created = orderProcessor.processOrder(customer.id());

        InvoiceDto fetched = invoiceService.getInvoiceByOrderId(created.orderId());

        assertThat(fetched.id()).isEqualTo(created.id());
        assertThat(fetched.orderId()).isEqualTo(created.orderId());
        assertThat(fetched.totalAmount()).isEqualByComparingTo(created.totalAmount());
        assertThat(fetched.customerName()).isEqualTo("Monika");
    }

    @Test
    void invoice_unknownOrderId_throws() {
        assertThatExceptionOfType(OrderNotFoundException.class).isThrownBy(() ->
            invoiceService.getInvoiceByOrderId(999L)
        );
    }

    @Test
    void getAllInvoices_returnsOnePerOrder() {
        ElectronicsDto product = newProduct("Mouse", new BigDecimal("120"), 10);
        CustomerDto c1 = newCustomer("Alice", "alice@example.com");
        CustomerDto c2 = newCustomer("Bob", "bob@example.com");

        cartService.addProduct(c1.id(), product.id(), ProductType.ELECTRONICS, 1);
        orderProcessor.processOrder(c1.id());

        cartService.addProduct(c2.id(), product.id(), ProductType.ELECTRONICS, 2);
        orderProcessor.processOrder(c2.id());

        List<InvoiceDto> all = invoiceService.getAllInvoices();
        assertThat(all).hasSize(2);
        assertThat(all).extracting(InvoiceDto::customerId).containsExactlyInAnyOrder(c1.id(), c2.id());
        assertThat(all).allSatisfy(inv -> assertThat(inv.issuedAt()).isNotNull());
    }

    @Test
    void order_timestamps_setCorrectlyInWarsawZone() {
        ElectronicsDto product = newProduct("SSD", new BigDecimal("300"), 5);
        CustomerDto customer = newCustomer("Zofia", "zofia@example.com");

        cartService.addProduct(customer.id(), product.id(), ProductType.ELECTRONICS, 1);
        InvoiceDto invoice = orderProcessor.processOrder(customer.id());

        OrderDto order = orderService.getOrderById(invoice.orderId());
        assertThat(order.createdAt()).isNotNull();
        assertThat(order.updatedAt()).isNotNull();
        assertThat(order.confirmedAt()).isNotNull();
        assertThat(order.createdAt().getZone().getId()).isEqualTo("Europe/Warsaw");
        assertThat(order.confirmedAt().getZone().getId()).isEqualTo("Europe/Warsaw");
        assertThat(order.confirmedAt()).isAfterOrEqualTo(order.createdAt());
    }

    @Test
    void getOrdersByCustomer_returnsOnlyThatCustomersOrders() {
        ElectronicsDto product = newProduct("Charger", new BigDecimal("80"), 20);
        CustomerDto alice = newCustomer("Alice", "alice@example.com");
        CustomerDto bob = newCustomer("Bob", "bob@example.com");

        cartService.addProduct(alice.id(), product.id(), ProductType.ELECTRONICS, 1);
        orderProcessor.processOrder(alice.id());

        cartService.addProduct(alice.id(), product.id(), ProductType.ELECTRONICS, 2);
        orderProcessor.processOrder(alice.id());

        cartService.addProduct(bob.id(), product.id(), ProductType.ELECTRONICS, 1);
        orderProcessor.processOrder(bob.id());

        List<OrderDto> aliceHistory = orderService.getOrdersByCustomer(alice.id());
        List<OrderDto> bobHistory = orderService.getOrdersByCustomer(bob.id());

        assertThat(aliceHistory).hasSize(2);
        assertThat(aliceHistory).allSatisfy(o -> {
            assertThat(o.customerId()).isEqualTo(alice.id());
            assertThat(o.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        });
        assertThat(bobHistory).hasSize(1);
        assertThat(bobHistory.get(0).customerId()).isEqualTo(bob.id());
    }

    @Test
    void getAllOrders_containsOrdersFromAllCustomers() {
        ElectronicsDto product = newProduct("Cable", new BigDecimal("30"), 20);
        CustomerDto c1 = newCustomer("C1", "c1@example.com");
        CustomerDto c2 = newCustomer("C2", "c2@example.com");
        CustomerDto c3 = newCustomer("C3", "c3@example.com");

        for (CustomerDto c : List.of(c1, c2, c3)) {
            cartService.addProduct(c.id(), product.id(), ProductType.ELECTRONICS, 1);
            orderProcessor.processOrder(c.id());
        }

        assertThat(orderService.getAllOrders()).hasSize(3);
    }

    @Test
    void cancelOrder_statusChangedToCANCELLED_updatedAtRefreshed() {
        ElectronicsDto product = newProduct("Monitor", new BigDecimal("900"), 5);
        CustomerDto customer = newCustomer("Bartek", "bartek@example.com");

        cartService.addProduct(customer.id(), product.id(), ProductType.ELECTRONICS, 1);
        InvoiceDto invoice = orderProcessor.processOrder(customer.id());

        OrderDto confirmed = orderService.getOrderById(invoice.orderId());
        assertThat(confirmed.orderStatus()).isEqualTo(OrderStatus.CONFIRMED);

        OrderDto cancelled = orderService.cancelOrder(invoice.orderId());

        assertThat(cancelled.orderStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelled.updatedAt()).isAfterOrEqualTo(confirmed.updatedAt());
        assertThat(cancelled.confirmedAt()).isNotNull(); // confirmedAt kept
    }

    @Test
    void cancelOrder_nonExistentId_throws() {
        assertThatExceptionOfType(OrderNotFoundException.class).isThrownBy(() -> orderService.cancelOrder(999L));
    }

    @Test
    void cancelOrder_invoiceStillExists_notDeleted() {
        ElectronicsDto product = newProduct("Keyboard", new BigDecimal("200"), 3);
        CustomerDto customer = newCustomer("Test", "test@example.com");

        cartService.addProduct(customer.id(), product.id(), ProductType.ELECTRONICS, 1);
        InvoiceDto invoice = orderProcessor.processOrder(customer.id());

        orderService.cancelOrder(invoice.orderId());

        InvoiceDto stillThere = invoiceService.getInvoiceByOrderId(invoice.orderId());
        assertThat(stillThere.id()).isEqualTo(invoice.id());
        assertThat(stillThere.totalAmount()).isEqualByComparingTo(new BigDecimal("200"));
    }
}
