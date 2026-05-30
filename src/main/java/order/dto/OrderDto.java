package order.dto;

import cart.dto.CartItemDto;
import cart.model.CartItem;
import order.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderDto(
        Long id,
        Long customerId,
        List<CartItemDto> items,
        BigDecimal totalPrice,
        LocalDateTime createdAt,
        OrderStatus orderStatus
) {
}
