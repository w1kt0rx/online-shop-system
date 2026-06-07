package cart.dto;

import product.model.Product;
import product.model.ProductType;

import java.math.BigDecimal;

public record CartItemDto(
        Long productId,
        String productName,
        ProductType productType,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal totalPrice
) {
}
