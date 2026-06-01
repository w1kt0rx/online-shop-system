package invoice.model;

import cart.dto.CartItemDto;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@ToString
public class Invoice {
    private final Long id;
    private final Long orderId;
    private final Long customerId;
    private final String customerName;
    private final List<CartItemDto> items;
    private final BigDecimal totalAmount;
    private final LocalDateTime issuedAt;

    public Invoice(Long id, Long orderId, Long customerId, String customerName,
                   List<CartItemDto> items, BigDecimal totalAmount) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.items = List.copyOf(items);
        this.totalAmount = totalAmount;
        this.issuedAt = LocalDateTime.now();
    }
}