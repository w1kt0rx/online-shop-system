package order.service;

import cart.mapper.CartMapper;
import cart.model.CartItem;
import customer.model.Customer;
import customer.repository.CustomerRepository;
import discount.service.DiscountService;
import discount.util.DiscountMessageFormatter;
import exception.CustomerNotFoundException;
import exception.EmptyCartException;
import exception.InsufficientStockException;
import exception.NotEnoughStockException;
import exception.OrderProcessingException;
import invoice.dto.InvoiceDto;
import invoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import order.model.Order;
import order.repository.OrderRepository;
import order.validator.OrderValidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates the full order placement pipeline:
 * validates the customer and cart, reserves stock, creates and confirms
 * the order, clears the cart, applies an optional discount, and delegates
 * invoice creation to InvoiceService.
 */
@RequiredArgsConstructor
public class OrderProcessor {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final DiscountService discountService;
    private final InvoiceService invoiceService;

    public InvoiceDto processOrder(Long customerId) {
        return processOrder(customerId, null);
    }

    public InvoiceDto processOrder(Long customerId, String discountCode) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new CustomerNotFoundException(customerId));

            OrderValidator.validateCart(customer.getCart());

            Order order = Order.of(
                    orderRepository.getNextId(),
                    customerId,
                    customer.getCart().getCartItems()
            );

            reserveStock(order.getItems());

            order.confirm();
            orderRepository.save(order);

            customer.getCart().clear();

            BigDecimal finalAmount = resolveTotal(order.getTotalPrice(), discountCode);

            return invoiceService.createInvoice(
                    order.getId(),
                    customer.getId(),
                    customer.getName(),
                    order.getItems().stream()
                            .map(CartMapper::toItemDto)
                            .toList(),
                    finalAmount
            );

        } catch (CustomerNotFoundException | EmptyCartException | InsufficientStockException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderProcessingException(
                    "Order processing failed for customer " + customerId, e
            );
        }
    }

    private BigDecimal resolveTotal(BigDecimal originalTotal, String discountCode) {
        if (discountCode == null || discountCode.isBlank()) {
            return originalTotal;
        }

        BigDecimal discounted = discountService.applyDiscount(discountCode, originalTotal);

        System.out.println(
                DiscountMessageFormatter.applied(
                        discountCode,
                        originalTotal,
                        discounted
                )
        );
        return discounted;
    }

    private void reserveStock(List<CartItem> items) {
        List<CartItem> reserved = new ArrayList<>();

        for (CartItem item : items) {
            var product = item.getProduct();
            try {
                product.decreaseQuantity(item.getQuantity());
            } catch (NotEnoughStockException e) {
                reserved.forEach(r -> r.getProduct().increaseQuantity(r.getQuantity()));
                throw new InsufficientStockException(
                        "Not enough stock for product: " + product.getName()
                                + ". Available: " + product.getQuantity()
                                + ", requested: " + item.getQuantity()
                );
            }
            reserved.add(item);
        }
    }
}