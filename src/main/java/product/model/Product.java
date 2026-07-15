package product.model;

import exception.InvalidProductException;
import exception.NotEnoughStockException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

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
    @Setter
    protected Long id;

    protected String name;
    protected BigDecimal basePrice;
    protected Integer quantity;

    @EqualsAndHashCode.Include
    protected ProductType productType;

    /**
     * Creates a new product after validating all fields.
     *
     * @param id          unique identifier; must be non-null and positive
     * @param name        product name; must be non-null and non-blank
     * @param price       base price; must be non-null and greater than zero
     * @param quantity    stock quantity; must be non-null and non-negative
     * @param productType category of the product
     */
    public Product(Long id, String name, BigDecimal price, Integer quantity, ProductType productType) {
        validate(id, name, price, quantity);

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
     * @throws IllegalArgumentException if amount is negative
     * @throws NotEnoughStockException  if amount exceeds current stock
     */
    public synchronized void decreaseQuantity(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        if (amount > quantity) {
            throw new NotEnoughStockException("Not enough stock available");
        }

        quantity -= amount;
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
        }

        quantity += amount;
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
        validateName(name);
        this.name = name;
    }

    /**
     * Updates the base price after validation.
     *
     * @param price new price; must be non-null and positive
     */
    public void updatePrice(BigDecimal price) {
        validatePrice(price);
        this.basePrice = price;
    }

    /**
     * Updates the stock quantity after validation.
     *
     * @param quantity new quantity; must be non-null and non-negative
     */
    public void updateQuantity(Integer quantity) {
        validateQuantity(quantity);
        this.quantity = quantity;
    }

    public static void validate(Long id, String name, BigDecimal price, Integer quantity) {
        List<String> errors = new ArrayList<>();

        errors.addAll(validateIdErrors(id));
        errors.addAll(validateNameErrors(name));
        errors.addAll(validatePriceErrors(price));
        errors.addAll(validateQuantityErrors(quantity));

        if (!errors.isEmpty()) {
            throw new InvalidProductException(String.join(", ", errors));
        }
    }

    public static void validateName(String name) {
        List<String> errors = validateNameErrors(name);
        if (!errors.isEmpty()) {
            throw new InvalidProductException(String.join(", ", errors));
        }
    }

    public static void validatePrice(BigDecimal price) {
        List<String> errors = validatePriceErrors(price);
        if (!errors.isEmpty()) {
            throw new InvalidProductException(String.join(", ", errors));
        }
    }

    public static void validateQuantity(Integer quantity) {
        List<String> errors = validateQuantityErrors(quantity);
        if (!errors.isEmpty()) {
            throw new InvalidProductException(String.join(", ", errors));
        }
    }

    private static List<String> validateIdErrors(Long id) {
        List<String> errors = new ArrayList<>();

        if (id != null && id < 0) {
            errors.add("Id cannot be negative");
        }

        return errors;
    }

    private static List<String> validateNameErrors(String name) {
        List<String> errors = new ArrayList<>();

        if (name == null || name.isBlank()) {
            errors.add("Name cannot be blank");
        }

        return errors;
    }

    private static List<String> validatePriceErrors(BigDecimal price) {
        List<String> errors = new ArrayList<>();

        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Price cannot be negative");
        }

        return errors;
    }

    private static List<String> validateQuantityErrors(Integer quantity) {
        List<String> errors = new ArrayList<>();

        if (quantity == null || quantity < 0) {
            errors.add("Quantity cannot be negative or null");
        }

        return errors;
    }
}
