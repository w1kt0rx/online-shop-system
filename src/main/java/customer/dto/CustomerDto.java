package customer.dto;

import cart.model.Cart;

public record CustomerDto(String name, Cart cart) {
}
