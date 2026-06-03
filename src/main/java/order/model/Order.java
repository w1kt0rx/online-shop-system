package order.model;

import cart.model.CartItem;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@ToString
public class Order {
    private final Long id;
    private final Long customerId;
    private final List<CartItem> items;
    private BigDecimal totalPrice;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confirmedAt;
    private OrderStatus status;

    public Order(Long id, Long customerId, List<CartItem> items) {
        this.id = id;
        this.customerId = customerId;
        this.items = List.copyOf(items);
        this.totalPrice = items.stream()
                .map(CartItem::calculateTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.status = OrderStatus.PENDING;
    }

    public void confirm() {
        this.status = OrderStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.updatedAt = this.confirmedAt;

    }

    public void cancel() {
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
}
