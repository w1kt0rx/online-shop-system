package order.dto;

import cart.dto.CartItemDto;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import order.model.OrderStatus;

public record OrderDto(
    Long id,
    Long customerId,
    List<CartItemDto> items,
    BigDecimal totalPrice,
    ZonedDateTime createdAt,
    ZonedDateTime updatedAt,
    ZonedDateTime confirmedAt,
    OrderStatus orderStatus
) {}
