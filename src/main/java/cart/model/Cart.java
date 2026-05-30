package cart.model;

import product.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Cart {
    private final List<CartItem> products = new ArrayList<>();

    public void addProduct(Product product, int quantity) {
        validateQuantity(quantity);

        Optional<CartItem> existingItem = findCartItem(product);

        if (existingItem.isPresent()) {
            existingItem.get().increaseQuantity(quantity);
            return;
        }

        products.add(new CartItem(product, quantity));
    }

    public void removeProduct(Product product) {
        products.removeIf(item -> item.getProduct().equals(product));
    }

    public void clear() {
        products.clear();
    }

    public boolean isEmpty() {
        return products.isEmpty();
    }

    public BigDecimal getTotalPrice() {
        return products.stream()
                .map(CartItem::calculateTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

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

