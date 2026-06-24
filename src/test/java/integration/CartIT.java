package integration;

import cart.dto.CartDto;
import cart.dto.CartItemDto;
import cart.service.CartService;
import customer.dto.CreateCustomerRequest;
import customer.dto.CustomerDto;
import customer.repository.impl.InMemoryCustomerRepository;
import customer.service.CustomerService;
import exception.InsufficientStockException;
import exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import product.dto.computer.ComputerDto;
import product.dto.computer.CreateComputerRequest;
import product.dto.electronics.CreateElectronicsRequest;
import product.dto.electronics.ElectronicsDto;
import product.dto.smartphone.CreateSmartphoneRequest;
import product.dto.smartphone.SmartphoneDto;
import product.model.ProductType;
import product.model.computer.configuration.*;
import product.model.smartphone.configuration.*;
import product.repository.impl.*;
import product.service.*;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for shopping cart operations — add, remove, accumulate,
 * configuration pricing, stock checks, and cart isolation between customers.
 */
class CartIT {

    private CartService cartService;
    private CustomerService customerService;
    private ComputerService computerService;
    private SmartphoneService smartphoneService;
    private ElectronicsService electronicsService;

    @BeforeEach
    void setUp() {
        InMemoryCustomerRepository customerRepo    = new InMemoryCustomerRepository();
        InMemoryComputerRepository computerRepo    = new InMemoryComputerRepository();
        InMemorySmartphoneRepository phoneRepo     = new InMemorySmartphoneRepository();
        InMemoryElectronicsRepository electronicsRepo = new InMemoryElectronicsRepository();

        customerService    = new CustomerService(customerRepo);
        computerService    = new ComputerService(computerRepo);
        smartphoneService  = new SmartphoneService(phoneRepo);
        electronicsService = new ElectronicsService(electronicsRepo);
        cartService        = new CartService(customerRepo, computerRepo, phoneRepo, electronicsRepo);
    }

    private CustomerDto newCustomer(String name, String email) {
        return customerService.createCustomer(
                new CreateCustomerRequest(name, email, "Password123"));
    }

    @Test
    void addElectronics_cartContainsItemWithCorrectPriceAndQuantity() {
        ElectronicsDto monitor = electronicsService.create(
                new CreateElectronicsRequest("Monitor 27\"", new BigDecimal("1200"), 10));
        CustomerDto customer = newCustomer("Jan", "jan@example.com");

        CartDto cart = cartService.addProduct(
                customer.id(), monitor.id(), ProductType.ELECTRONICS, 3);

        assertThat(cart.items()).hasSize(1);
        CartItemDto item = cart.items().get(0);
        assertThat(item.productName()).isEqualTo("Monitor 27\"");
        assertThat(item.quantity()).isEqualTo(3);
        assertThat(item.unitPrice()).isEqualByComparingTo(new BigDecimal("1200"));
        assertThat(item.totalPrice()).isEqualByComparingTo(new BigDecimal("3600"));
        assertThat(cart.totalPrice()).isEqualByComparingTo(new BigDecimal("3600"));
    }

    @Test
    void addComputer_cartReflectsConfigurationPrice() {
        // base 2000 + i9(1200) + 32GB(800) + SSD2TB(900) + RTX4070(2000) = 6900
        ComputerDto computer = computerService.create(new CreateComputerRequest(
                "Gaming PC", new BigDecimal("2000"), 5,
                Processor.INTEL_I9, Ram.RAM_32GB, StorageType.SSD_2TB, GraphicsCard.RTX_4070));
        CustomerDto customer = newCustomer("Anna", "anna@example.com");

        CartDto cart = cartService.addProduct(
                customer.id(), computer.id(), ProductType.COMPUTER, 1);

        assertThat(cart.items()).hasSize(1);
        assertThat(cart.items().get(0).unitPrice()).isEqualByComparingTo(new BigDecimal("6900"));
        assertThat(cart.totalPrice()).isEqualByComparingTo(new BigDecimal("6900"));
    }

    @Test
    void addSmartphone_cartReflectsColorBatteryAndAccessoryPrices() {
        // base 3000 + GOLD(100) + BAT_6000(500) + CHARGER(120) + HEADPHONES(200) = 3920
        SmartphoneDto phone = smartphoneService.create(new CreateSmartphoneRequest(
                "Pixel 8 Pro", new BigDecimal("3000"), 10,
                Set.of(Accessory.CHARGER, Accessory.HEADPHONES),
                BatteryCapacity.BATTERY_6000, Color.GOLD));
        CustomerDto customer = newCustomer("Ewa", "ewa@example.com");

        CartDto cart = cartService.addProduct(
                customer.id(), phone.id(), ProductType.SMARTPHONE, 2);

        assertThat(cart.items().get(0).unitPrice()).isEqualByComparingTo(new BigDecimal("3920"));
        assertThat(cart.totalPrice()).isEqualByComparingTo(new BigDecimal("7840"));
    }

    @Test
    void addSameProductTwice_quantityAccumulated_notDuplicated() {
        ElectronicsDto keyboard = electronicsService.create(
                new CreateElectronicsRequest("Keyboard", new BigDecimal("300"), 20));
        CustomerDto customer = newCustomer("Piotr", "piotr@example.com");

        cartService.addProduct(customer.id(), keyboard.id(), ProductType.ELECTRONICS, 2);
        CartDto cart = cartService.addProduct(
                customer.id(), keyboard.id(), ProductType.ELECTRONICS, 3);

        assertThat(cart.items()).hasSize(1);
        assertThat(cart.items().get(0).quantity()).isEqualTo(5);
        assertThat(cart.totalPrice()).isEqualByComparingTo(new BigDecimal("1500"));
    }

    @Test
    void addMultipleDifferentProducts_totalIsSumOfAll() {
        ElectronicsDto monitor = electronicsService.create(
                new CreateElectronicsRequest("Monitor", new BigDecimal("1000"), 5));
        ElectronicsDto keyboard = electronicsService.create(
                new CreateElectronicsRequest("Keyboard", new BigDecimal("300"), 10));
        ElectronicsDto mouse = electronicsService.create(
                new CreateElectronicsRequest("Mouse", new BigDecimal("150"), 15));
        CustomerDto customer = newCustomer("Maria", "maria@example.com");

        cartService.addProduct(customer.id(), monitor.id(), ProductType.ELECTRONICS, 2);
        cartService.addProduct(customer.id(), keyboard.id(), ProductType.ELECTRONICS, 1);
        CartDto cart = cartService.addProduct(
                customer.id(), mouse.id(), ProductType.ELECTRONICS, 4);

        assertThat(cart.items()).hasSize(3);
        // 2×1000 + 1×300 + 4×150 = 2900
        assertThat(cart.totalPrice()).isEqualByComparingTo(new BigDecimal("2900"));
    }

    @Test
    void addProduct_exceedingStock_throws_cartUnchanged() {
        ElectronicsDto item = electronicsService.create(
                new CreateElectronicsRequest("SSD", new BigDecimal("400"), 3));
        CustomerDto customer = newCustomer("Test", "test@example.com");

        assertThatExceptionOfType(InsufficientStockException.class)
                .isThrownBy(() -> cartService.addProduct(
                        customer.id(), item.id(), ProductType.ELECTRONICS, 5));

        assertThat(cartService.getCart(customer.id()).items()).isEmpty();
    }

    @ParameterizedTest(name = "stock={0}, request={1}")
    @CsvSource({"1, 2", "3, 4", "0, 1"})
    void addProduct_variousOverstockScenarios_alwaysThrows(int stock, int requested) {
        ElectronicsDto item = electronicsService.create(
                new CreateElectronicsRequest("Item", new BigDecimal("100"), stock));
        CustomerDto customer = newCustomer("Test" + stock, "test" + stock + "@example.com");

        assertThatExceptionOfType(InsufficientStockException.class)
                .isThrownBy(() -> cartService.addProduct(
                        customer.id(), item.id(), ProductType.ELECTRONICS, requested));
    }

    @Test
    void addProduct_unknownProductId_throws() {
        CustomerDto customer = newCustomer("Test", "test@example.com");

        assertThatExceptionOfType(ProductNotFoundException.class)
                .isThrownBy(() -> cartService.addProduct(
                        customer.id(), 999L, ProductType.ELECTRONICS, 1));
    }

    @Test
    void removeProduct_cartUpdatedCorrectly() {
        ElectronicsDto monitor = electronicsService.create(
                new CreateElectronicsRequest("Monitor", new BigDecimal("1000"), 5));
        ElectronicsDto keyboard = electronicsService.create(
                new CreateElectronicsRequest("Keyboard", new BigDecimal("300"), 10));
        CustomerDto customer = newCustomer("User", "user@example.com");

        cartService.addProduct(customer.id(), monitor.id(), ProductType.ELECTRONICS, 1);
        cartService.addProduct(customer.id(), keyboard.id(), ProductType.ELECTRONICS, 2);
        CartDto after = cartService.removeProduct(
                customer.id(), monitor.id(), ProductType.ELECTRONICS);

        assertThat(after.items()).hasSize(1);
        assertThat(after.items().get(0).productName()).isEqualTo("Keyboard");
        assertThat(after.totalPrice()).isEqualByComparingTo(new BigDecimal("600"));
    }

    @Test
    void clearCart_removesAllItems_totalIsZero() {
        ElectronicsDto a = electronicsService.create(
                new CreateElectronicsRequest("A", new BigDecimal("100"), 5));
        ElectronicsDto b = electronicsService.create(
                new CreateElectronicsRequest("B", new BigDecimal("200"), 5));
        CustomerDto customer = newCustomer("User", "user@example.com");

        cartService.addProduct(customer.id(), a.id(), ProductType.ELECTRONICS, 2);
        cartService.addProduct(customer.id(), b.id(), ProductType.ELECTRONICS, 3);
        CartDto empty = cartService.clearCart(customer.id());

        assertThat(empty.items()).isEmpty();
        assertThat(empty.totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void twoCustomers_independentCarts_noLeakBetweenThem() {
        ElectronicsDto product = electronicsService.create(
                new CreateElectronicsRequest("Monitor", new BigDecimal("800"), 20));
        CustomerDto alice = newCustomer("Alice", "alice@example.com");
        CustomerDto bob   = newCustomer("Bob",   "bob@example.com");

        cartService.addProduct(alice.id(), product.id(), ProductType.ELECTRONICS, 3);
        cartService.addProduct(bob.id(),   product.id(), ProductType.ELECTRONICS, 1);

        CartDto aliceCart = cartService.getCart(alice.id());
        CartDto bobCart   = cartService.getCart(bob.id());

        assertThat(aliceCart.items().get(0).quantity()).isEqualTo(3);
        assertThat(aliceCart.totalPrice()).isEqualByComparingTo(new BigDecimal("2400"));

        assertThat(bobCart.items().get(0).quantity()).isEqualTo(1);
        assertThat(bobCart.totalPrice()).isEqualByComparingTo(new BigDecimal("800"));
    }

    @Test
    void getCart_emptyByDefault() {
        CustomerDto customer = newCustomer("Empty", "empty@example.com");

        CartDto cart = cartService.getCart(customer.id());

        assertThat(cart.items()).isEmpty();
        assertThat(cart.totalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}