package entity;

import exception.InvalidIdException;
import exception.InvalidNameException;
import exception.InvalidPriceException;
import exception.InvalidQuantityException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter

public abstract class Product {
    private final Long id;
    private final String name;
    @Setter
    private BigDecimal basePrice;
    private int quantity;

    public Product(Long id, String name, BigDecimal price, int quantity) {
        validateId(id);
        validateName(name);
        validatePrice(price);
        validateQuantity(quantity);

        this.id = id;
        this.name = name;
        this.basePrice = price;
        this.quantity = quantity;
    }

    public void decreaseQuantity(int amount) {
        if(amount > quantity || amount < 0) {
            throw new InvalidQuantityException("Quantity cannot be negative");
        } else {
            quantity -= amount;
        }
    }

    public void increaseQuantity(int amount) {
        if(amount <= 0) {
            throw new IllegalArgumentException("Amount added must be bigger than 0");
        } else {
            quantity += amount;
        }
    }

    public boolean isAvailable() {
        return quantity > 0;
    }

    private void validateId(Long id) {
        if (id == null || id < 0) {
            throw new InvalidIdException("Id cannot be empty or less than zero");
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidNameException("Name cannot be empty");
        }
    }

    private void validatePrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPriceException("Price cannot be null or less than zero");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity < 0) {
            throw new InvalidQuantityException("Quantity cannot be less than zero");
        }
    }

}
