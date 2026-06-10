package cart.mapper;

import cart.dto.CartDto;
import cart.dto.CartItemDto;
import cart.model.Cart;
import cart.model.CartItem;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CartMapper {

    public static CartDto toDto(Cart cart) {
        List<CartItemDto> items = cart.getCartItems().stream()
                .map(CartMapper::toItemDto)
                .toList();
        return new CartDto(items, cart.getTotalPrice());
    }

    public static CartItemDto toItemDto(CartItem item) {
        return new CartItemDto(
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getProduct().getProductType(),
                item.getProduct().getPrice(),
                item.getQuantity(),
                item.calculateTotalPrice()
        );
    }
}