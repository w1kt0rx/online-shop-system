package cart.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import cart.dto.CartDto;
import customer.model.Customer;
import customer.repository.CustomerRepository;
import exception.CustomerNotFoundException;
import exception.InsufficientStockException;
import exception.InvalidProductException;
import exception.ProductNotFoundException;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.model.ProductType;
import product.model.computer.Computer;
import product.model.computer.configuration.ComputerConfiguration;
import product.model.electronics.Electronics;
import product.model.smartphone.Smartphone;
import product.model.smartphone.configuration.SmartphoneConfiguration;
import product.repository.ComputerRepository;
import product.repository.ElectronicsRepository;
import product.repository.SmartphoneRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ComputerRepository computerRepository;

    @Mock
    private SmartphoneRepository smartphoneRepository;

    @Mock
    private ElectronicsRepository electronicsRepository;

    @InjectMocks
    private CartService cartService;

    private Customer customer;
    private Computer computer;
    private Smartphone smartphone;
    private Electronics electronics;

    @BeforeEach
    void setup() {
        customer = new Customer(1L, "Jan Kowalski", "wiktor@gmail.com", "Password!123");
        computer = new Computer(1L, "Dell XPS", new BigDecimal("3000"), 10, new ComputerConfiguration());
        smartphone = new Smartphone(1L, "iPhone 15", new BigDecimal("4000"), 5, new SmartphoneConfiguration());
        electronics = new Electronics(1L, "Monitor", new BigDecimal("800"), 20);
    }

    @Test
    void shouldAddComputerToCart() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(computerRepository.findById(1L)).thenReturn(Optional.of(computer));

        CartDto result = cartService.addProduct(1L, 1L, ProductType.COMPUTER, 2);

        assertEquals(1, result.items().size());
        assertEquals("Dell XPS", result.items().get(0).productName());
        assertEquals(2, result.items().get(0).quantity());
    }

    @Test
    void shouldAddSmartphoneToCart() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(smartphone));

        CartDto result = cartService.addProduct(1L, 1L, ProductType.SMARTPHONE, 1);

        assertEquals(1, result.items().size());
        assertEquals("iPhone 15", result.items().get(0).productName());
    }

    @Test
    void shouldAddElectronicsToCart() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(electronicsRepository.findById(1L)).thenReturn(Optional.of(electronics));

        CartDto result = cartService.addProduct(1L, 1L, ProductType.ELECTRONICS, 3);

        assertEquals(1, result.items().size());
        assertEquals("Monitor", result.items().get(0).productName());
        assertEquals(3, result.items().get(0).quantity());
    }

    @Test
    void shouldIncreaseQuantityWhenAddingSameProductTwice() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(computerRepository.findById(1L)).thenReturn(Optional.of(computer));

        cartService.addProduct(1L, 1L, ProductType.COMPUTER, 2);
        CartDto result = cartService.addProduct(1L, 1L, ProductType.COMPUTER, 3);

        assertEquals(1, result.items().size());
        assertEquals(5, result.items().get(0).quantity());
    }

    @Test
    void shouldRemoveProductFromCart() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(computerRepository.findById(1L)).thenReturn(Optional.of(computer));

        cartService.addProduct(1L, 1L, ProductType.COMPUTER, 2);
        CartDto result = cartService.removeProduct(1L, 1L, ProductType.COMPUTER);

        assertTrue(result.items().isEmpty());
        assertEquals(BigDecimal.ZERO, result.totalPrice());
    }

    @Test
    void shouldReturnCorrectTotalPrice() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(computerRepository.findById(1L)).thenReturn(Optional.of(computer));
        when(electronicsRepository.findById(1L)).thenReturn(Optional.of(electronics));

        cartService.addProduct(1L, 1L, ProductType.COMPUTER, 1);
        CartDto result = cartService.addProduct(1L, 1L, ProductType.ELECTRONICS, 2);

        // computer default config price: 3000 + 500+200+300+0 = 4000; electronics: 800*2 = 1600
        assertEquals(new BigDecimal("5600"), result.totalPrice());
    }

    @Test
    void shouldClearCart() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(computerRepository.findById(1L)).thenReturn(Optional.of(computer));

        cartService.addProduct(1L, 1L, ProductType.COMPUTER, 2);
        CartDto result = cartService.clearCart(1L);

        assertTrue(result.items().isEmpty());
    }

    @Test
    void shouldThrowWhenCustomerNotFound() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class, () -> cartService.addProduct(99L, 1L, ProductType.COMPUTER, 1));
    }

    @Test
    void shouldThrowWhenProductNotFound() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(computerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> cartService.addProduct(1L, 99L, ProductType.COMPUTER, 1));
    }

    @Test
    void shouldThrowWhenRequestedQuantityExceedsStock() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(computerRepository.findById(1L)).thenReturn(Optional.of(computer));

        assertThrows(InsufficientStockException.class, () -> cartService.addProduct(1L, 1L, ProductType.COMPUTER, 999));
    }

    @Test
    void shouldGetCartForCustomer() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        CartDto result = cartService.getCart(1L);

        assertNotNull(result);
        assertTrue(result.items().isEmpty());
        assertEquals(BigDecimal.ZERO, result.totalPrice());
    }
}
