package repository;

import entity.Product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryProductRepository implements ProductRepository {

    private final Map<Long, Product> products = new HashMap<>();

    @Override
    public Product save(Product product) {
        products.put(product.getId(), product);

        return product;
    }

    @Override
    public void deleteById(Long id) {
        products.remove(id);
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public List<Product> getAll() {
        return products.values().stream().toList();
    }
}
