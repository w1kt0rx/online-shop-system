package order.service;

import customer.repository.CustomerRepository;
import exception.CustomerNotFoundException;
import exception.OrderNotFoundException;
import lombok.RequiredArgsConstructor;
import order.dto.OrderDto;
import order.mapper.OrderMapper;
import order.model.Order;
import order.repository.OrderRepository;

import java.util.List;

@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;

    public OrderDto getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with id " + id + " not found"
                ));
        return OrderMapper.toDto(order);
    }

    public List<OrderDto> getAllOrders() {
        return orderRepository.getAll().stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    public List<OrderDto> getOrdersByCustomer(Long customerId) {
        customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer with id " + customerId + " not found"
                ));
        return orderRepository.getAll().stream()
                .filter(order -> order.getCustomerId().equals(customerId))
                .map(OrderMapper::toDto)
                .toList();
    }

    public OrderDto cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with id " + orderId + " not found"
                ));
        order.cancel();
        return OrderMapper.toDto(orderRepository.save(order));
    }
}