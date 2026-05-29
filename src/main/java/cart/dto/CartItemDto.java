package cart.dto;

import product.model.Product;

public record CartItemDto(Product product,
                          Integer quantity
                          ) {
}
