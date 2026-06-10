package cart.validator;

import exception.InvalidProductException;
import exception.NotEnoughStockException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import product.model.Product;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CartItemValidator {

    public static void validateCartItem(Product product, Integer quantity) {
        validateProduct(product);
        validateQuantity(quantity);
    }

    public static void validateProduct(Product product) {
        if (product == null) {
            throw new InvalidProductException("Product cannot be null");
        }
    }

    public static void validateQuantity(Integer amount) {
        if (amount <= 0 ) {
            throw new NotEnoughStockException("Quantity cannot be negative");
        }
    }
}
