package customer.dto;

import cart.dto.CartDto;
import cart.model.Cart;

public record CustomerDto(
        Long id,
        String name,
        CartDto cart
) {
}