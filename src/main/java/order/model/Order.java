package order.model;

import cart.model.CartItem;
import common.time.ShopClock;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Represents a placed order in the system.
 * An order is created from a customer's cart, capturing a snapshot of the
 * items and computing the total price at creation time. Its lifecycle is
 * driven by status transitions: PENDING → CONFIRMED or
 * PENDING → CANCELLED.
 */
@Getter
@ToString
public class Order {
    private final Long id;
    private final Long customerId;
    private final List<CartItem> items;
    private BigDecimal totalPrice;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime confirmedAt;
    private OrderStatus status;

    /**
     * Creates a new order in rderStatus#PENDING state.
     * The total price is computed as the sum of all item totals.
     *
     * @param id         unique order identifier
     * @param customerId identifier of the customer who placed the order
     * @param items      snapshot of the cart items; stored as an immutable copy
     */
    public Order(Long id, Long customerId, List<CartItem> items) {
        this.id = id;
        this.customerId = customerId;
        this.items = List.copyOf(items);
        this.totalPrice = items.stream()
                .map(CartItem::calculateTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.createdAt = ShopClock.now();
        this.updatedAt = this.createdAt;
        this.status = OrderStatus.PENDING;
    }

    /**
     * Transitions the order to OrderStatus#CONFIRMED and records the confirmation timestamp.
     */
    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = ShopClock.now();
        this.updatedAt = this.confirmedAt;

    }

    /**
     * Transitions the order to OrderStatus#CANCELLED and updates the last-modified timestamp.
     */
    public void cancel() {
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = ShopClock.now();
    }
}
