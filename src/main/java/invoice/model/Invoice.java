package invoice.model;

import cart.dto.CartItemDto;
import common.time.TimeUtils;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class Invoice {

    @Setter
    private Long id;

    private final Long orderId;
    private final Long customerId;
    private final String customerName;
    private final List<CartItemDto> items;
    private final BigDecimal totalAmount;
    private final ZonedDateTime issuedAt;

    public Invoice(
        Long id,
        Long orderId,
        Long customerId,
        String customerName,
        List<CartItemDto> items,
        BigDecimal totalAmount
    ) {
        this.id = id;
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.items = List.copyOf(items);
        this.totalAmount = totalAmount;
        this.issuedAt = TimeUtils.now();
    }
}
