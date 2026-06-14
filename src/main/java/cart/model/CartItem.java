package cart.model;

import cart.validator.CartItemValidator;
import exception.InvalidProductException;
import exception.NotEnoughStockException;
import product.model.Product;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CartItem {
    private final Product product;
    private Integer quantity;

    public CartItem(Product product, Integer quantity) {
        CartItemValidator.validateCartItem(product, quantity);
        this.product = product;
        this.quantity = quantity;
    }

    public BigDecimal calculateTotalPrice() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public void increaseQuantity(int amount) {
        CartItemValidator.validateQuantity(amount);
        quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        if (quantity - amount < 0 || amount < 0) {
            throw new NotEnoughStockException("Quantity cannot be negative");
        }
        quantity -= amount;
    }

}

