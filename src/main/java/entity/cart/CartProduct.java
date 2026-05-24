package entity.cart;

import entity.Product;
import exception.InvalidProductException;
import exception.InvalidQuantityException;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class CartProduct {
    private final Product product;
    private int quantity;

    public CartProduct(Product product, int quantity) {
        validateProduct(product);
        validateQuantity(quantity);
        this.product = product;
        this.quantity = quantity;
    }

    public BigDecimal calculateTotalPrice(){
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    public void increaseQuantity(int amount) {
        validateQuantity(amount);
        quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        if(quantity - amount < 0 || amount < 0) {
            throw new InvalidQuantityException("Quantity cannot be negative");
        }
        quantity -= amount;
    }
    private void validateQuantity(int amount) {
        if(amount <= 0) {
            throw new InvalidQuantityException("Quantity cannot be negative");
        }
    }

    private void validateProduct(Product product) {
        if(product == null) {
            throw new InvalidProductException("Product cannot be null");
        }
    }
}
