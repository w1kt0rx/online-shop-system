package order.service;

import cart.mapper.CartMapper;
import customer.model.Customer;
import customer.repository.CustomerRepository;
import exception.CustomerNotFoundException;
import exception.EmptyCardException;
import exception.InsufficientStockException;
import exception.OrderNotFoundException;
import invoice.mapper.InvoiceMapper;
import invoice.model.Invoice;
import invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import order.model.Order;
import order.repository.OrderRepository;
import order.validator.OrderValidator;
import invoice.dto.InvoiceDto;

import java.util.List;

@RequiredArgsConstructor
public class OrderProcessor {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;

    public InvoiceDto processOrder(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer with id " + customerId + " not found"));

        OrderValidator.validateCart(customer.getCart());
        validateStock(customer);

        Order order = new Order(
                orderRepository.getNextId(),
                customerId,
                customer.getCart().getProducts()
        );

        order.getItems().forEach(item ->
                item.getProduct().decreaseQuantity(item.getQuantity())
        );

        orderRepository.save(order);
        order.confirm();

        customer.getCart().clear();

        Invoice invoice = new Invoice(
                invoiceRepository.getNextId(),
                order.getId(),
                customer.getId(),
                customer.getName(),
                order.getItems().stream().map(CartMapper::toItemDto).toList(),
                order.getTotalPrice()
        );

        return InvoiceMapper.toDto(invoiceRepository.save(invoice));
    }

    public InvoiceDto getInvoiceByOrderId(Long orderId) {
        return invoiceRepository.getAll().stream()
                .filter(inv -> inv.getOrderId().equals(orderId))
                .findFirst()
                .map(InvoiceMapper::toDto)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Invoice for order " + orderId + " not found"));
    }

    public List<InvoiceDto> getAllInvoices() {
        return invoiceRepository.getAll().stream()
                .map(InvoiceMapper::toDto)
                .toList();
    }

    private void validateStock(Customer customer) {
        customer.getCart().getProducts().forEach(item -> {
            if (item.getQuantity() > item.getProduct().getQuantity()) {
                throw new InsufficientStockException(
                        "Not enough stock for product: " + item.getProduct().getName()
                                + ". Available: " + item.getProduct().getQuantity()
                                + ", requested: " + item.getQuantity()
                );
            }
        });
    }
}
