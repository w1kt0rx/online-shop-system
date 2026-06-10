package integration;

import cart.dto.CartDto;
import customer.dto.CreateCustomerRequest;
import customer.dto.CustomerDto;
import customer.repository.impl.InMemoryCustomerRepository;
import customer.service.CustomerService;
import discount.dto.CreateDiscountRequest;
import discount.dto.DiscountDto;
import discount.model.DiscountType;
import discount.repository.impl.InMemoryDiscountRepository;
import discount.service.DiscountService;
import cart.service.CartService;
import exception.CustomerNotFoundException;
import exception.EmptyCartException;
import exception.InsufficientStockException;
import invoice.dto.InvoiceDto;
import invoice.repository.impl.InMemoryInvoiceRepository;
import order.model.OrderProcessingResult;
import order.repository.impl.InMemoryOrderRepository;
import order.service.AsyncOrderProcessor;
import order.service.ConcurrentOrderProcessor;
import order.service.OrderProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import product.dto.computer.ComputerDto;
import product.dto.computer.CreateComputerRequest;
import product.dto.electronics.CreateElectronicsRequest;
import product.dto.electronics.ElectronicsDto;
import product.dto.smartphone.CreateSmartphoneRequest;
import product.dto.smartphone.SmartphoneDto;
import product.facade.ProductService;
import product.model.ProductType;
import product.model.computer.configuration.GraphicsCard;
import product.model.computer.configuration.Processor;
import product.model.computer.configuration.Ram;
import product.model.computer.configuration.StorageType;
import product.model.smartphone.configuration.Accessory;
import product.model.smartphone.configuration.BatteryCapacity;
import product.model.smartphone.configuration.Color;
import product.repository.impl.InMemoryComputerRepository;
import product.repository.impl.InMemoryElectronicsRepository;
import product.repository.impl.InMemorySmartphoneRepository;
import product.service.ComputerService;
import product.service.ElectronicsService;
import product.service.SmartphoneService;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

class ShopIntegrationTest {

    private InMemoryCustomerRepository customerRepository;
    private InMemoryComputerRepository computerRepository;
    private InMemorySmartphoneRepository smartphoneRepository;
    private InMemoryElectronicsRepository electronicsRepository;
    private InMemoryOrderRepository orderRepository;
    private InMemoryInvoiceRepository invoiceRepository;
    private InMemoryDiscountRepository discountRepository;

    private CustomerService customerService;
    private ComputerService computerService;
    private SmartphoneService smartphoneService;
    private ElectronicsService electronicsService;
    private DiscountService discountService;
    private CartService cartService;
    private OrderProcessor orderProcessor;
    private ConcurrentOrderProcessor concurrentOrderProcessor;
    private AsyncOrderProcessor asyncOrderProcessor;
    private ProductService productFacade;

    @BeforeEach
    void setUp() {
        customerRepository    = new InMemoryCustomerRepository();
        computerRepository    = new InMemoryComputerRepository();
        smartphoneRepository  = new InMemorySmartphoneRepository();
        electronicsRepository = new InMemoryElectronicsRepository();
        orderRepository       = new InMemoryOrderRepository();
        invoiceRepository     = new InMemoryInvoiceRepository();
        discountRepository    = new InMemoryDiscountRepository();

        customerService    = new CustomerService(customerRepository);
        computerService    = new ComputerService(computerRepository);
        smartphoneService  = new SmartphoneService(smartphoneRepository);
        electronicsService = new ElectronicsService(electronicsRepository);
        discountService    = new DiscountService(discountRepository);

        cartService = new CartService(
                customerRepository, computerRepository,
                smartphoneRepository, electronicsRepository
        );

        orderProcessor = new OrderProcessor(
                orderRepository, customerRepository,
                invoiceRepository, discountService
        );

        concurrentOrderProcessor = new ConcurrentOrderProcessor(orderProcessor, 4);

        asyncOrderProcessor = new AsyncOrderProcessor(orderProcessor, 4);

        productFacade = new ProductService(
                computerService, smartphoneService, electronicsService,
                discountService, concurrentOrderProcessor, asyncOrderProcessor
        );
    }


    @Test
    void shouldCreateAndRetrieveComputer() {
        ComputerDto created = productFacade.createComputer(new CreateComputerRequest(
                "Dell XPS 15", new BigDecimal("5000"), 10,
                Processor.INTEL_I7, Ram.RAM_16GB, StorageType.SSD_1TB, GraphicsCard.RTX_3050
        ));

        ComputerDto fetched = productFacade.getComputerById(created.id());

        assertThat(fetched.id()).isEqualTo(created.id());
        assertThat(fetched.name()).isEqualTo("Dell XPS 15");
        assertThat(productFacade.getAllComputers()).hasSize(1);
    }

    @Test
    void shouldCreateAndRetrieveSmartphone() {
        SmartphoneDto created = productFacade.createSmartphone(new CreateSmartphoneRequest(
                "Samsung Galaxy S24", new BigDecimal("3500"), 20,
                Set.of(Accessory.CHARGER, Accessory.PHONE_CASE), BatteryCapacity.BATTERY_5000, Color.BLACK
        ));

        SmartphoneDto fetched = productFacade.getSmartphoneById(created.id());

        assertThat(fetched.name()).isEqualTo("Samsung Galaxy S24");
        assertThat(productFacade.getAllSmartphones()).hasSize(1);
    }

    @Test
    void shouldCreateAndRetrieveElectronics() {
        ElectronicsDto created = productFacade.createElectronics(
                new CreateElectronicsRequest("Sony TV 55\"", new BigDecimal("2500"), 5)
        );

        ElectronicsDto fetched = productFacade.getElectronicsById(created.id());

        assertThat(fetched.name()).isEqualTo("Sony TV 55\"");
        assertThat(productFacade.getAllElectronics()).hasSize(1);
    }

    @Test
    void shouldDeleteProduct() {
        ElectronicsDto tv = productFacade.createElectronics(
                new CreateElectronicsRequest("Soundbar", new BigDecimal("800"), 3)
        );

        productFacade.deleteElectronics(tv.id());

        assertThat(productFacade.getAllElectronics()).isEmpty();
    }


    @Test
    void shouldCreateAndRetrieveCustomer() {
        CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest("Anna Nowak"));

        CustomerDto fetched = customerService.getCustomerById(customer.id());

        assertThat(fetched.id()).isEqualTo(customer.id());
        assertThat(fetched.name()).isEqualTo("Anna Nowak");
    }

    @Test
    void shouldDeleteCustomer() {
        CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest("Jan Kowalski"));

        customerService.deleteCustomer(customer.id());

        assertThatExceptionOfType(CustomerNotFoundException.class)
                .isThrownBy(() -> customerService.getCustomerById(customer.id()));
    }

    @Test
    void shouldThrowWhenCustomerNotFound() {
        assertThatExceptionOfType(CustomerNotFoundException.class)
                .isThrownBy(() -> customerService.getCustomerById(999L));
    }


    @Test
    void shouldAddProductToCart() {
        CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest("Piotr Wiśniewski"));
        ElectronicsDto monitor = productFacade.createElectronics(
                new CreateElectronicsRequest("Monitor 27\"", new BigDecimal("1200"), 5)
        );

        CartDto cart = cartService.addProduct(customer.id(), monitor.id(), ProductType.ELECTRONICS, 2);

        assertThat(cart.items()).hasSize(1);
        assertThat(cart.items().get(0).quantity()).isEqualTo(2);
    }

    @Test
    void shouldRemoveProductFromCart() {
        CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest("Marta Kowalczyk"));
        ElectronicsDto speaker = productFacade.createElectronics(
                new CreateElectronicsRequest("Speakers", new BigDecimal("300"), 10)
        );

        cartService.addProduct(customer.id(), speaker.id(), ProductType.ELECTRONICS, 1);
        CartDto afterRemove = cartService.removeProduct(customer.id(), speaker.id(), ProductType.ELECTRONICS);

        assertThat(afterRemove.items()).isEmpty();
    }

    @Test
    void shouldRejectQuantityExceedingStock() {
        CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest("Tomasz Malinowski"));
        ElectronicsDto limited = productFacade.createElectronics(
                new CreateElectronicsRequest("Projector", new BigDecimal("4000"), 2)
        );

        assertThatExceptionOfType(InsufficientStockException.class)
                .isThrownBy(() -> cartService.addProduct(customer.id(), limited.id(), ProductType.ELECTRONICS, 5));
    }

    @Test
    void shouldClearCart() {
        CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest("Agnieszka Zając"));
        ElectronicsDto headphones = productFacade.createElectronics(
                new CreateElectronicsRequest("Headphones", new BigDecimal("600"), 10)
        );

        cartService.addProduct(customer.id(), headphones.id(), ProductType.ELECTRONICS, 3);
        CartDto cleared = cartService.clearCart(customer.id());

        assertThat(cleared.items()).isEmpty();
    }


    @Test
    void shouldCreateAndRetrievePercentageDiscount() {
        discountService.createDiscount(new CreateDiscountRequest(
                "SUMMER10", "Summer discount 10%", DiscountType.PERCENTAGE, new BigDecimal("10"),
                new BigDecimal("500"), ZonedDateTime.now().minusDays(1), ZonedDateTime.now().plusDays(30)
        ));

        DiscountDto fetched = discountService.getByCode("SUMMER10");

        assertThat(fetched.code()).isEqualTo("SUMMER10");
        assertThat(fetched.type()).isEqualTo(DiscountType.PERCENTAGE);
    }

    @Test
    void shouldApplyPercentageDiscount() {
        discountService.createDiscount(new CreateDiscountRequest(
                "SAVE20", "20% cheaper", DiscountType.PERCENTAGE, new BigDecimal("20"),
                BigDecimal.ZERO, ZonedDateTime.now().minusDays(1), ZonedDateTime.now().plusDays(30)
        ));

        BigDecimal result = discountService.applyDiscount("SAVE20", new BigDecimal("1000"));

        assertThat(result).isEqualByComparingTo(new BigDecimal("800.00"));
    }

    @Test
    void shouldApplyFixedAmountDiscount() {
        discountService.createDiscount(new CreateDiscountRequest(
                "MINUS100", "100 zł cheaper", DiscountType.FIXED_AMOUNT, new BigDecimal("100"),
                BigDecimal.ZERO, ZonedDateTime.now().minusDays(1), ZonedDateTime.now().plusDays(30)
        ));

        BigDecimal result = discountService.applyDiscount("MINUS100", new BigDecimal("500"));

        assertThat(result).isEqualByComparingTo(new BigDecimal("400.00"));
    }

    @Test
    void shouldNotReturnDeactivatedDiscount() {
        discountService.createDiscount(new CreateDiscountRequest(
                "ACTIVE", "Active", DiscountType.PERCENTAGE, new BigDecimal("5"),
                BigDecimal.ZERO, ZonedDateTime.now().minusDays(1), ZonedDateTime.now().plusDays(10)
        ));
        DiscountDto toDeactivate = discountService.createDiscount(new CreateDiscountRequest(
                "INACTIVE", "Deactivated", DiscountType.PERCENTAGE, new BigDecimal("15"),
                BigDecimal.ZERO, ZonedDateTime.now().minusDays(1), ZonedDateTime.now().plusDays(10)
        ));

        discountService.deActivate(toDeactivate.id());

        assertThat(discountService.getAllActive())
                .extracting(DiscountDto::code)
                .containsOnly("ACTIVE")
                .doesNotContain("INACTIVE");
    }


    @Test
    void shouldProcessFullOrderFlow() {
        CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest("Krzysztof Nowak"));
        ElectronicsDto laptop = productFacade.createElectronics(
                new CreateElectronicsRequest("Laptop Lenovo", new BigDecimal("3000"), 5)
        );

        cartService.addProduct(customer.id(), laptop.id(), ProductType.ELECTRONICS, 2);
        InvoiceDto invoice = orderProcessor.processOrder(customer.id());

        assertThat(invoice).isNotNull();
        assertThat(invoice.customerId()).isEqualTo(customer.id());
        assertThat(invoice.customerName()).isEqualTo("Krzysztof Nowak");
        assertThat(invoice.totalAmount()).isEqualByComparingTo(new BigDecimal("6000"));
        assertThat(invoice.items()).hasSize(1);
    }

    @Test
    void shouldDecreaseStockAfterOrder() {
        CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest("Beata Wiśniewska"));
        ElectronicsDto printer = productFacade.createElectronics(
                new CreateElectronicsRequest("Pinter HP", new BigDecimal("800"), 10)
        );

        cartService.addProduct(customer.id(), printer.id(), ProductType.ELECTRONICS, 3);
        orderProcessor.processOrder(customer.id());

        assertThat(productFacade.getElectronicsById(printer.id()).quantity()).isEqualTo(7);
    }

    @Test
    void shouldClearCartAfterOrder() {
        CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest("Rafał Kaczmarek"));
        ElectronicsDto webcam = productFacade.createElectronics(
                new CreateElectronicsRequest("Camera", new BigDecimal("350"), 8)
        );

        cartService.addProduct(customer.id(), webcam.id(), ProductType.ELECTRONICS, 1);
        orderProcessor.processOrder(customer.id());

        assertThat(cartService.getCart(customer.id()).items()).isEmpty();
    }

    @Test
    void shouldThrowForEmptyCart() {
        CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest("Dorota Szymańska"));

        assertThatExceptionOfType(EmptyCartException.class)
                .isThrownBy(() -> orderProcessor.processOrder(customer.id()));
    }

    @Test
    void shouldThrowForNonExistentCustomer() {
        assertThatExceptionOfType(CustomerNotFoundException.class)
                .isThrownBy(() -> orderProcessor.processOrder(999L));
    }

    @Test
    void shouldApplyDiscountDuringOrder() {
        discountService.createDiscount(new CreateDiscountRequest(
                "PROMO15", "15% discount", DiscountType.PERCENTAGE, new BigDecimal("15"),
                BigDecimal.ZERO, ZonedDateTime.now().minusDays(1), ZonedDateTime.now().plusDays(30)
        ));
        CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest("Łukasz Pawlak"));
        ElectronicsDto router = productFacade.createElectronics(
                new CreateElectronicsRequest("Router WiFi 6", new BigDecimal("500"), 5)
        );

        cartService.addProduct(customer.id(), router.id(), ProductType.ELECTRONICS, 2);
        InvoiceDto invoice = orderProcessor.processOrder(customer.id(), "PROMO15");

        // 2 × 500 = 1000, substracting 15% = 850
        assertThat(invoice.totalAmount()).isEqualByComparingTo(new BigDecimal("850.00"));
    }

    @Test
    void shouldFinalizeOrderWithInvalidDiscountCode() {
        CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest("Monika Lewandowska"));
        ElectronicsDto keyboard = productFacade.createElectronics(
                new CreateElectronicsRequest("Mechanical keyboard", new BigDecimal("400"), 5)
        );

        cartService.addProduct(customer.id(), keyboard.id(), ProductType.ELECTRONICS, 1);
        InvoiceDto invoice = orderProcessor.processOrder(customer.id(), "NIEISTNIEJE");

        assertThat(invoice).isNotNull();
        assertThat(invoice.totalAmount()).isEqualByComparingTo(new BigDecimal("400"));
    }

    @Test
    void shouldRetrieveInvoiceByOrderId() {
        CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest("Paweł Czyżewski"));
        ElectronicsDto hub = productFacade.createElectronics(
                new CreateElectronicsRequest("Hub USB-C", new BigDecimal("200"), 10)
        );

        cartService.addProduct(customer.id(), hub.id(), ProductType.ELECTRONICS, 1);
        InvoiceDto created = orderProcessor.processOrder(customer.id());

        InvoiceDto fetched = orderProcessor.getInvoiceByOrderId(created.orderId());

        assertThat(fetched.id()).isEqualTo(created.id());
        assertThat(fetched.totalAmount()).isEqualByComparingTo(created.totalAmount());
    }

    @Test
    void shouldHandleMixedCartWithComputerAndSmartphone() {
        CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest("Ewelina Baran"));
        ComputerDto computer = productFacade.createComputer(new CreateComputerRequest(
                "MacBook Pro", new BigDecimal("6000"), 3,
                Processor.INTEL_I9, Ram.RAM_32GB, StorageType.SSD_2TB, GraphicsCard.INTEGRATED
        ));
        SmartphoneDto phone = productFacade.createSmartphone(new CreateSmartphoneRequest(
                "iPhone 15", new BigDecimal("4500"), 5,
                Set.of(Accessory.CABLE), BatteryCapacity.BATTERY_4000, Color.WHITE
        ));

        cartService.addProduct(customer.id(), computer.id(), ProductType.COMPUTER, 1);
        cartService.addProduct(customer.id(), phone.id(), ProductType.SMARTPHONE, 1);
        InvoiceDto invoice = orderProcessor.processOrder(customer.id());

        assertThat(invoice.items()).hasSize(2);
        // Computer: 6000 + 1200 (i9) + 800 (32GB) + 900 (SSD 2TB) + 0 (integrated) = 8900
        // Smartphone:  4500 + 40 (cable) + 150 (battery 4000) + 50 (white) = 4740
        assertThat(invoice.totalAmount()).isEqualByComparingTo(new BigDecimal("13640"));
    }


    @Test
    void shouldProcessAllOrdersConcurrently() {
        ElectronicsDto product = productFacade.createElectronics(
                new CreateElectronicsRequest("Pendrive 128GB", new BigDecimal("100"), 50)
        );

        List<Long> customerIds = List.of(
                createCustomerWithProduct("Client A", product.id(), 1),
                createCustomerWithProduct("Client B", product.id(), 1),
                createCustomerWithProduct("Client C", product.id(), 1)
        );

        List<OrderProcessingResult> results = productFacade.processBatchOrders(customerIds);

        assertThat(results).hasSize(3);
        assertThat(results).allSatisfy(r -> assertThat(r.success()).isTrue());
    }

    @Test
    void shouldReturnFailureForCustomerWithEmptyCart() {
        ElectronicsDto product = productFacade.createElectronics(
                new CreateElectronicsRequest("SSD 1TB", new BigDecimal("400"), 10)
        );
        long withCart = createCustomerWithProduct("With cart", product.id(), 1);
        CustomerDto withoutCart = customerService.createCustomer(new CreateCustomerRequest("Without car"));

        List<OrderProcessingResult> results = productFacade.processBatchOrders(
                List.of(withCart, withoutCart.id())
        );

        assertThat(results.stream().filter(OrderProcessingResult::success).count()).isEqualTo(1);
        assertThat(results.stream().filter(r -> !r.success()).count()).isEqualTo(1);
    }

    @Test
    void shouldNotOversellProductUnderConcurrency() {
        ElectronicsDto limited = productFacade.createElectronics(
                new CreateElectronicsRequest("Limited Edition", new BigDecimal("1000"), 5)
        );
        List<Long> customerIds = List.of(
                createCustomerWithProduct("K1", limited.id(), 1),
                createCustomerWithProduct("K2", limited.id(), 1),
                createCustomerWithProduct("K3", limited.id(), 1),
                createCustomerWithProduct("K4", limited.id(), 1),
                createCustomerWithProduct("K5", limited.id(), 1)
        );

        productFacade.processBatchOrders(customerIds);

        assertThat(productFacade.getElectronicsById(limited.id()).quantity()).isGreaterThanOrEqualTo(0);
    }

    private long createCustomerWithProduct(String name, Long productId, int quantity) {
        CustomerDto customer = customerService.createCustomer(new CreateCustomerRequest(name));
        cartService.addProduct(customer.id(), productId, ProductType.ELECTRONICS, quantity);
        return customer.id();
    }
}