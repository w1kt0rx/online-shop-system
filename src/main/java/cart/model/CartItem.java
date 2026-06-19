package cart.model;

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
        validateProduct(product);
        validateQuantity(quantity);

        this.product = product;
        this.quantity = quantity;
    }

    public BigDecimal calculateTotalPrice() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public void increaseQuantity(int amount) {
        validateQuantity(amount);
        quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        if (quantity - amount < 0 || amount < 0) {
            throw new NotEnoughStockException("Quantity cannot be negative");
        }
        quantity -= amount;
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }
    }
    private void validateProduct(Product product) {
        if (product == null) {
            throw new InvalidProductException("Product cannot be null");
        }
    }

}


