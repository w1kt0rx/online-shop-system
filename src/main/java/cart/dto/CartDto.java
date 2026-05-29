package cart.dto;

import java.util.List;

public record CartDto(List<CartItemDto> items) {
}
