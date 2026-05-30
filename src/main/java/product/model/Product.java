package product.model;

import exception.NotEnoughStockException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import product.validator.ProductValidator;

import java.math.BigDecimal;

@Getter
@EqualsAndHashCode
public abstract class Product {
    protected Long id;
    protected String name;
    protected BigDecimal basePrice;
    protected Integer quantity;
    protected ProductType productType;

    public Product(Long id, String name, BigDecimal price, Integer quantity, ProductType productType) {
        ProductValidator.validate(id, name, price, quantity);

        this.id = id;
        this.name = name;
        this.basePrice = price;
        this.quantity = quantity;
        this.productType = productType;
    }

    public BigDecimal getPrice() {
        return basePrice;
    }

    public void decreaseQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (amount > quantity) {
            throw new NotEnoughStockException("Not enough stock available");
        } else {
            quantity -= amount;
        }
    }

    public void increaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        } else {
            quantity += amount;
        }
    }

    public boolean isAvailable() {
        return quantity > 0;
    }

    public void updateName(String name) {
        ProductValidator.validateName(name);
        this.name = name;
    }

    public void updatePrice(BigDecimal price) {
        ProductValidator.validatePrice(price);
        this.basePrice = price;
    }

    public void updateQuantity(Integer quantity) {
        ProductValidator.validateQuantity(quantity);
        this.quantity = quantity;
    }

}
