package product.model;

import exception.NotEnoughStockException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import product.validator.ProductValidator;

import java.math.BigDecimal;

/**
 * Abstract base class for all products in the shop.
 * <p>
 * Holds common attributes (id, name, price, quantity, type) and enforces
 * validation on construction. Subclasses represent concrete product categories
 * such as Computer, Smartphone, and Electronics.
 * </p>
 */
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public abstract class Product {
    @EqualsAndHashCode.Include
    protected Long id;
    protected String name;
    protected BigDecimal basePrice;
    protected Integer quantity;
    @EqualsAndHashCode.Include
    protected ProductType productType;


    /**
     * Creates a new product after validating all fields via ProductValidator.
     *
     * @param id          unique identifier; must be non-null and positive
     * @param name        product name; must be non-null and non-blank
     * @param price       base price; must be non-null and greater than zero
     * @param quantity    stock quantity; must be non-null and non-negative
     * @param productType category of the product
     */
    public Product(Long id, String name, BigDecimal price, Integer quantity, ProductType productType) {
        ProductValidator.validate(id, name, price, quantity);

        this.id = id;
        this.name = name;
        this.basePrice = price;
        this.quantity = quantity;
        this.productType = productType;
    }

    /**
     * Returns the effective selling price. Subclasses may override this to apply
     * category-specific pricing logic.
     *
     * @return the base price by default
     */
    public BigDecimal getPrice() {
        return basePrice;
    }

    /**
     * Reduces stock by amount.
     *
     * @param amount number of units to deduct; must be non-negative
     * @throws IllegalArgumentException  if amount is negative
     * @throws NotEnoughStockException   if amount exceeds current stock
     */
    public synchronized void decreaseQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (amount > quantity) {
            throw new NotEnoughStockException("Not enough stock available");
        } else {
            quantity -= amount;
        }
    }

    /**
     * Increases stock by amount.
     *
     * @param amount number of units to add; must be greater than zero
     * @throws IllegalArgumentException if amount is not positive
     */
    public synchronized void increaseQuantity(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        } else {
            quantity += amount;
        }
    }

    /**
     * Returns true if at least one unit is in stock.
     *
     * @return true when quantity is greater than zero
     */
    public boolean isAvailable() {
        return quantity > 0;
    }

    /**
     * Updates the product name after validation.
     *
     * @param name new name; must be non-null and non-blank
     */
    public void updateName(String name) {
        ProductValidator.validateName(name);
        this.name = name;
    }

    /**
     * Updates the base price after validation.
     *
     * @param price new price; must be non-null and positive
     */
    public void updatePrice(BigDecimal price) {
        ProductValidator.validatePrice(price);
        this.basePrice = price;
    }

    /**
     * Updates the stock quantity after validation.
     *
     * @param quantity new quantity; must be non-null and non-negative
     */
    public void updateQuantity(Integer quantity) {
        ProductValidator.validateQuantity(quantity);
        this.quantity = quantity;
    }

}
