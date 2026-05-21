package repository;

import entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    public Product save(Product product);

    public void deleteById(Long id);

    public Optional<Product> findById(Long id);

    public List<Product> getAll();
}
