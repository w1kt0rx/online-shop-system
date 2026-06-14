package cart.validator;

import exception.InvalidProductException;
import exception.NotEnoughStockException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import product.model.Product;

/**
 * Utility class for validating cart.model.CartItem data before creation.
 *
 * <p>Validation failures throw InvalidProductException — the input
 * data is invalid, not the stock level. NotEnoughStockException is
 * reserved for cases where the product exists but the requested quantity
 * exceeds available stock.</p>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CartItemValidator {

    /**
     * Validates both the product reference and the requested quantity.
     *
     * @param product  the product to add; must not be null
     * @param quantity the number of units; must be greater than zero
     * @throws InvalidProductException if product is null or quantity is not positive
     */
    public static void validateCartItem(Product product, Integer quantity) {
        validateProduct(product);
        validateQuantity(quantity);
    }

    /**
     * Validates that the product reference is not null.
     *
     * @param product the product to validate
     * @throws InvalidProductException if product is null
     */
    public static void validateProduct(Product product) {
        if (product == null) {
            throw new InvalidProductException("Product cannot be null");
        }
    }

    /**
     * Validates that the quantity is a positive integer.
     *
     * @param amount the quantity to validate
     * @throws InvalidProductException if amount is zero or negative
     */
    public static void validateQuantity(Integer amount) {
        if (amount <= 0) {
            throw new InvalidProductException("Quantity must be greater than zero, but was: " + amount);
        }
    }
}
