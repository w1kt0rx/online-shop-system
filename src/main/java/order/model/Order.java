package order.model;

import cart.model.CartItem;
import common.time.ShopClock;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
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
     * Creates a new order in OrderStatus#PENDING state.
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
     * Full-state constructor used to reconstruct an order from persisted data.
     * Bypasses total-price calculation and timestamp generation, taking every
     * field as-is.
     */
    private Order(Long id, Long customerId, List<CartItem> items, BigDecimal totalPrice,
                  ZonedDateTime createdAt, ZonedDateTime updatedAt, ZonedDateTime confirmedAt,
                  OrderStatus status) {
        this.id = id;
        this.customerId = customerId;
        this.items = List.copyOf(items);
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.confirmedAt = confirmedAt;
        this.status = status;
    }

    /**
     * Reconstructs an order from a previously persisted snapshot, e.g. when reloading
     * orders from {@code FileOrderRepository} on application restart.
     * <p>
     * The original cart items are not part of the persisted snapshot, so the
     * reconstructed order's {@link #getItems()} is empty; all other fields
     * (total price, timestamps, status) reflect the persisted state exactly.
     * </p>
     *
     * @param id          the persisted order id
     * @param customerId  the persisted customer id
     * @param totalPrice  the persisted order total
     * @param createdAt   the persisted creation timestamp
     * @param updatedAt   the persisted last-update timestamp
     * @param confirmedAt the persisted confirmation timestamp; may be null
     * @param status      the persisted order status
     * @return a reconstructed Order with an empty item list
     */
    public static Order restore(Long id, Long customerId, BigDecimal totalPrice,
                                ZonedDateTime createdAt, ZonedDateTime updatedAt,
                                ZonedDateTime confirmedAt, OrderStatus status) {
        return new Order(id, customerId, List.of(), totalPrice, createdAt, updatedAt, confirmedAt, status);
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
