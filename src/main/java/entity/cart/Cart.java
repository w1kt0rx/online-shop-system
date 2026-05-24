package entity.cart;

import entity.Product;
import exception.InvalidProductException;
import exception.InvalidQuantityException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Cart {
    private final List<CartProduct> products = new ArrayList<>();
    public void addProduct(Product product, int quantity) {

        validateProduct(product);
        validateQuantity(quantity);

        Optional<CartProduct> existingItem =
                findCartItem(product);

        if (existingItem.isPresent()) {

            existingItem.get()
                    .increaseQuantity(quantity);

            return;
        }

        products.add(
                new CartProduct(product, quantity)
        );
    }

    public void removeProduct(Product product) {

        products.removeIf(item ->
                item.getProduct().equals(product)
        );
    }

    public BigDecimal getTotalPrice() {

        return products.stream()
                .map(CartProduct::calculateTotalPrice)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    public List<CartProduct> getProducts() {

        return List.copyOf(products);
    }

    private Optional<CartProduct> findCartItem(
            Product product
    ) {

        return products.stream()
                .filter(item ->
                        item.getProduct()
                                .equals(product))
                .findFirst();
    }

    private void validateProduct(Product product) {
        if(product == null) {
            throw new InvalidProductException("Product cannot be null");
        }
    }

    private void validateQuantity(int quantity) {

        if (quantity <= 0) {
            throw new InvalidQuantityException(
                    "Quantity must be greater than zero"
            );
        }
    }



}
