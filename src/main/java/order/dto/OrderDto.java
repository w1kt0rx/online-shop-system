package order.dto;

import cart.dto.CartItemDto;
import order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public record OrderDto(
        Long id,
        Long customerId,
        List<CartItemDto> items,
        BigDecimal totalPrice,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt,
        ZonedDateTime confirmedAt,
        OrderStatus orderStatus
) {
}
