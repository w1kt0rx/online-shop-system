package order.model;

import cart.model.CartItem;
import common.time.TimeUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
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
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Order {
    private final Long id;
    private final Long customerId;
    private final List<CartItem> items;
    private BigDecimal totalPrice;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ZonedDateTime confirmedAt;
    private OrderStatus status;

    public static Order of(Long id, Long customerId, List<CartItem> items) {
        BigDecimal price = items.stream()
                .map(CartItem::calculateTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new Order(id, customerId, items, price, TimeUtils.now(), TimeUtils.now(), null, OrderStatus.PENDING );
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
        this.confirmedAt = TimeUtils.now();
        this.updatedAt = this.confirmedAt;

    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Transitions the order to OrderStatus#CANCELLED and updates the last-modified timestamp.
     */
    public void cancel() {
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = TimeUtils.now();
    }
}
