package cart.model;

import product.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a customer's shopping cart.
 * Maintains an ordered list of {@link CartItem} entries. Adding the same product
 * multiple times merges quantities rather than creating duplicate entries.
 */
public class Cart {
    private final List<CartItem> products = new ArrayList<>();

    /**
     * Adds a product to the cart, or increases the quantity of an existing entry.
     *
     * @param product  the product to add; must not be null
     * @param quantity number of units; must be greater than zero
     * @throws IllegalArgumentException if quantity is not positive
     */
    public void addProduct(Product product, int quantity) {
        validateQuantity(quantity);

        Optional<CartItem> existingItem = findCartItem(product);

        if (existingItem.isPresent()) {
            existingItem.get().increaseQuantity(quantity);
            return;
        }

        products.add(new CartItem(product, quantity));
    }

    /**
     * Removes all cart entries for the given product.
     *
     * @param product the product to remove
     */
    public void removeProduct(Product product) {
        products.removeIf(item -> item.getProduct().equals(product));
    }

    /**
     * Removes all items from the cart.
     */
    public void clear() {
        products.clear();
    }

    /**
     * Returns true if the cart contains no items.
     *
     * @return true when empty
     */
    public boolean isEmpty() {
        return products.isEmpty();
    }

    /**
     * Calculates the total price for all items in the cart.
     *
     * @return sum of each CartItem price, or zero
     * if the cart is empty
     */
    public BigDecimal getTotalPrice() {
        return products.stream()
                .map(CartItem::calculateTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Returns an unmodifiable snapshot of the current cart items.
     *
     * @return immutable copy of the item list
     */
    public List<CartItem> getProducts() {
        return List.copyOf(products);
    }

    private Optional<CartItem> findCartItem(Product product) {
        return products.stream()
                .filter(item -> item.getProduct().equals(product))
                .findFirst();
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be bigger than zero");
        }
    }
}

