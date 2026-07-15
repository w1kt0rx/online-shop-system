package order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import customer.model.Customer;
import customer.repository.CustomerRepository;
import exception.CustomerNotFoundException;
import exception.EmptyCartException;
import exception.OrderNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
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

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private OrderService orderService;

    private Customer customerWithItems;
    private Customer emptyCustomer;
    private Electronics product;

    @BeforeEach
    void setup() {
        product = new Electronics(1L, "Monitor", new BigDecimal("800"), 10);

        customerWithItems = new Customer(1L, "Anna", "fdfdsfsfds@gmail.com", "Password");
        customerWithItems.getCart().addProduct(product, 2);

        emptyCustomer = new Customer(2L, "Anna", "fdfdsfsfds@gmail.com", "Password");
    }

    @Test
    void shouldGetOrderById() {
        Order order = Order.of(1L, 1L, customerWithItems.getCart().getCartItems());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderDto result = orderService.getOrderById(1L);

        assertEquals(1L, result.id());
        assertEquals(1L, result.customerId());
    }

    @Test
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(99L));
    }

    @Test
    void shouldGetAllOrders() {
        Order order1 = Order.of(1L, 1L, customerWithItems.getCart().getCartItems());
        Order order2 = Order.of(2L, 1L, customerWithItems.getCart().getCartItems());
        when(orderRepository.getAll()).thenReturn(List.of(order1, order2));

        List<OrderDto> result = orderService.getAllOrders();

        assertEquals(2, result.size());
    }

    @Test
    void shouldGetOrdersByCustomer() {
        Order order1 = Order.of(1L, 1L, customerWithItems.getCart().getCartItems());
        Order order2 = Order.of(2L, 2L, customerWithItems.getCart().getCartItems());
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customerWithItems));
        when(orderRepository.getAll()).thenReturn(List.of(order1, order2));

        List<OrderDto> result = orderService.getOrdersByCustomer(1L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).customerId());
    }

    @Test
    void shouldCancelOrder() {
        Order order = Order.of(1L, 1L, customerWithItems.getCart().getCartItems());
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        OrderDto result = orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, result.orderStatus());
    }

    @Test
    void shouldThrowWhenCancellingNonExistentOrder() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrder(99L));
    }
}
