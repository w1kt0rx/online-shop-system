package service;

import entity.Product;
import exception.InvalidProductException;
import exception.ProductNotFoundException;
import repository.ProductRepository;

public class ProductManager {
    private final ProductRepository repository;

    public ProductManager(ProductRepository repository) {
        this.repository = repository;
    }

    public Product addProduct(Product product) {
        validateProduct(product);

        return repository.save(product);
    }

    public Product getProduct(Long id) {
        return repository.findById(id).orElseThrow(() -> new ProductNotFoundException("Product not found"));
    }

    private void validateProduct(Product product) {
        if(product == null) {
            throw new InvalidProductException("Product cannot be null");
        }
    }


}
