package cart.dto;

import java.math.BigDecimal;
import product.model.Product;
import product.model.ProductType;

public record CartItemDto(
    Long productId,
    String productName,
    ProductType productType,
    BigDecimal unitPrice,
    Integer quantity,
    BigDecimal totalPrice
) {}
