package order.service;

import customer.model.Customer;
import customer.repository.CustomerRepository;
import exception.CustomerNotFoundException;
import exception.EmptyCardException;
import exception.OrderNotFoundException;
import order.dto.OrderDto;
import order.model.Order;
import order.model.OrderStatus;
import order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.model.electronics.Electronics;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private CustomerRepository customerRepository;

    @InjectMocks
    private OrderService orderService;

    private Customer customerWithItems;
    private Customer emptyCustomer;
    private Electronics product;

    @BeforeEach
    void setup() {
        product = new Electronics(1L, "Monitor", new BigDecimal("800"), 10);

        customerWithItems = new Customer(1L, "Jan Kowalski");
        customerWithItems.getCart().addProduct(product, 2);

        emptyCustomer = new Customer(2L, "Anna Nowak");
    }

    @Test
    void shouldPlaceOrderSuccessfully() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customerWithItems));
        when(orderRepository.getNextId()).thenReturn(1L);

        Order savedOrder = new Order(1L, 1L, customerWithItems.getCart().getProducts());
        when(orderRepository.save(any())).thenReturn(savedOrder);

        OrderDto result = orderService.placeOrder(1L);

        assertNotNull(result);
        assertEquals(1L, result.customerId());
        assertEquals(1, result.items().size());
        assertEquals(OrderStatus.PENDING, result.orderStatus());
        verify(orderRepository).save(any());
    }

    @Test
    void shouldClearCartAfterPlacingOrder() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customerWithItems));
        when(orderRepository.getNextId()).thenReturn(1L);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.placeOrder(1L);

        assertTrue(customerWithItems.getCart().isEmpty());
    }

    @Test
    void shouldDecreaseStockAfterPlacingOrder() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customerWithItems));
        when(orderRepository.getNextId()).thenReturn(1L);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        orderService.placeOrder(1L);

        // stock was 10, ordered 2 → should be 8
        assertEquals(8, product.getQuantity());
    }

    @Test
    void shouldCalculateCorrectTotalPrice() {
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customerWithItems));
        when(orderRepository.getNextId()).thenReturn(1L);
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderDto result = orderService.placeOrder(1L);

        // 2 × 800 = 1600
        assertEquals(new BigDecimal("1600"), result.totalPrice());
    }

    @Test
    void shouldThrowWhenPlacingOrderWithEmptyCart() {
        when(customerRepository.findById(2L)).thenReturn(Optional.of(emptyCustomer));

        assertThrows(EmptyCardException.class,
                () -> orderService.placeOrder(2L));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenCustomerNotFoundForOrder() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomerNotFoundException.class,
                () -> orderService.placeOrder(99L));
    }

    @Test
    void shouldGetOrderById() {
        Order order = new Order(1L, 1L, customerWithItems.getCart().getProducts());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderDto result = orderService.getOrderById(1L);

        assertEquals(1L, result.id());
        assertEquals(1L, result.customerId());
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrderById(99L));
    }

    @Test
    void shouldGetAllOrders() {
        Order order1 = new Order(1L, 1L, customerWithItems.getCart().getProducts());
        Order order2 = new Order(2L, 1L, customerWithItems.getCart().getProducts());
        when(orderRepository.getAll()).thenReturn(List.of(order1, order2));

        List<OrderDto> result = orderService.getAllOrders();

        assertEquals(2, result.size());
    }

    @Test
    void shouldGetOrdersByCustomer() {
        Order order1 = new Order(1L, 1L, customerWithItems.getCart().getProducts());
        Order order2 = new Order(2L, 2L, customerWithItems.getCart().getProducts());
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customerWithItems));
        when(orderRepository.getAll()).thenReturn(List.of(order1, order2));

        List<OrderDto> result = orderService.getOrdersByCustomer(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).customerId());
    }

    @Test
    void shouldCancelOrder() {
        Order order = new Order(1L, 1L, customerWithItems.getCart().getProducts());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderDto result = orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, result.orderStatus());
    }

    @Test
    void shouldThrowWhenCancellingNonExistentOrder() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.cancelOrder(99L));
    }
}