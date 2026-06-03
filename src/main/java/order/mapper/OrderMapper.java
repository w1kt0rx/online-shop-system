package order.mapper;

import cart.mapper.CartMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import order.dto.OrderDto;
import order.model.Order;

import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OrderMapper {

    public static OrderDto toDto(Order order) {
        return new OrderDto(
                order.getId(),
                order.getCustomerId(),
                order.getItems().stream()
                        .map(CartMapper::toItemDto)
                        .collect(Collectors.toList()),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                order.getConfirmedAt(),
                order.getStatus()
        );
    }
}