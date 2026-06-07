package order.service;

import cart.mapper.CartMapper;
import customer.model.Customer;
import customer.repository.CustomerRepository;
import discount.service.DiscountService;
import exception.*;
import invoice.mapper.InvoiceMapper;
import invoice.model.Invoice;
import invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import order.model.Order;
import order.repository.OrderRepository;
import order.validator.OrderValidator;
import invoice.dto.InvoiceDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Orchestrates the full order-to-invoice pipeline.
 * <p>
 * Responsibilities include: validating the customer and their cart, checking stock
 * availability, persisting the order, decrementing product stock, optionally applying
 * a discount code, and generating a final InvoiceDto.
 * </p>
 * <p>
 * This class delegates discount logic to DiscountService and follows a
 * fail-fast approach — domain exceptions are re-thrown as-is while unexpected
 * failures are wrapped in OrderProcessingException.
 * </p>
 */
@RequiredArgsConstructor
public class OrderProcessor {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final InvoiceRepository invoiceRepository;
    private final DiscountService discountService;

    /**
     * Processes an order for the given customer without a discount.
     * Equivalent to calling processOrder(Long, String) with null.
     *
     * @param customerId identifier of the customer placing the order
     * @return the generated invoice DTO
     * @throws CustomerNotFoundException  if no customer exists with that id
     * @throws EmptyCartException         if the customer's cart is empty
     * @throws InsufficientStockException if any item exceeds available stock
     * @throws OrderProcessingException   if an unexpected error occurs
     */
    public InvoiceDto processOrder(Long customerId) {
        return processOrder(customerId, null);
    }

    /**
     * Processes an order for the given customer, applying an optional discount code.
     * <p>
     * Execution steps:
     * Load and validate the customer
     * Validate cart (non-empty) and stock levels
     * Persist the order and confirm it
     * Decrement product stock for each ordered item
     * Clear the customer's cart
     * Apply discount code if provided (failures are logged, not propagated)
     * Create and persist the invoice
     * </p>
     *
     * @param customerId   identifier of the customer placing the order
     * @param discountCode optional discount code; null or blank skips discounting
     * @return the generated invoice DTO
     * @throws CustomerNotFoundException  if no customer exists with that id
     * @throws EmptyCartException         if the customer's cart is empty
     * @throws InsufficientStockException if any item exceeds available stock
     * @throws OrderProcessingException   if an unexpected error occurs during processing
     */
    public InvoiceDto processOrder(Long customerId, String discountCode) {
        try {
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

            BigDecimal finalAmount = resolveTotal(order.getTotalPrice(), discountCode);

            Invoice invoice = new Invoice(
                    invoiceRepository.getNextId(),
                    order.getId(),
                    customer.getId(),
                    customer.getName(),
                    order.getItems().stream().map(CartMapper::toItemDto).toList(),
                    finalAmount
            );

            return InvoiceMapper.toDto(invoiceRepository.save(invoice));
        } catch (CustomerNotFoundException | EmptyCartException | InsufficientStockException e) {
            throw e;
        } catch (Exception e) {
            throw new OrderProcessingException(
                    "Order processing failed for customer " + customerId, e
            );
        }
    }

    /**
     * Retrieves the invoice associated with a given order.
     *
     * @param orderId the order identifier
     * @return the invoice DTO
     * @throws OrderNotFoundException if no invoice exists for that order id
     */
    public InvoiceDto getInvoiceByOrderId(Long orderId) {
        return invoiceRepository.getAll().stream()
                .filter(inv -> inv.getOrderId().equals(orderId))
                .findFirst()
                .map(InvoiceMapper::toDto)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Invoice for order " + orderId + " not found"));
    }

    /**
     * Returns all invoices stored in the system.
     *
     * @return list of all invoice DTOs; empty list if none exist
     */
    public List<InvoiceDto> getAllInvoices() {
        return invoiceRepository.getAll().stream()
                .map(InvoiceMapper::toDto)
                .toList();
    }

    /**
     * Resolves the final order total, applying a discount if a valid code is provided.
     * If the code is blank or discount application fails, the original total is returned.
     */
    private BigDecimal resolveTotal(BigDecimal originalTotal, String discountCode) {
        if (discountCode == null || discountCode.isBlank()) {
            return originalTotal;
        }
        try {
            BigDecimal discounted = discountService.applyDiscount(discountCode, originalTotal);
            System.out.printf("[Discount] Applied '%s': %.2f zł → %.2f zł%n",
                    discountCode, originalTotal.doubleValue(), discounted.doubleValue());
            return discounted;
        } catch (Exception e) {
            System.err.println("[Discount] Code '" + discountCode + "' could not be applied: " + e.getMessage());
            return originalTotal;
        }
    }

    /**
     * Validates that every cart item has sufficient stock available.
     *
     * @throws InsufficientStockException if any item's requested quantity exceeds available stock
     */
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
