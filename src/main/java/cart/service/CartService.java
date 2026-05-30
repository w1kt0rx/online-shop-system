package cart.service;

import cart.dto.CartDto;
import cart.mapper.CartMapper;
import customer.model.Customer;
import customer.repository.CustomerRepository;
import exception.CustomerNotFoundException;
import exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import product.model.Product;
import product.model.ProductType;
import product.repository.ComputerRepository;
import product.repository.ElectronicsRepository;
import product.repository.SmartphoneRepository;

@RequiredArgsConstructor
public class CartService {

    private final CustomerRepository customerRepository;
    private final ComputerRepository computerRepository;
    private final SmartphoneRepository smartphoneRepository;
    private final ElectronicsRepository electronicsRepository;

    public CartDto addProduct(Long customerId, Long productId, ProductType productType, Integer quantity) {
        Customer customer = findCustomer(customerId);
        Product product = findProduct(productId, productType);

        if (quantity > product.getQuantity()) {
            throw new IllegalArgumentException(
                    "Requested quantity " + quantity + " exceeds available stock " + product.getQuantity()
            );
        }

        customer.getCart().addProduct(product, quantity);
        return CartMapper.toDto(customer.getCart());
    }

    public CartDto removeProduct(Long customerId, Long productId, ProductType productType) {
        Customer customer = findCustomer(customerId);
        Product product = findProduct(productId, productType);

        customer.getCart().removeProduct(product);
        return CartMapper.toDto(customer.getCart());
    }

    public CartDto getCart(Long customerId) {
        Customer customer = findCustomer(customerId);
        return CartMapper.toDto(customer.getCart());
    }

    public CartDto clearCart(Long customerId) {
        Customer customer = findCustomer(customerId);
        customer.getCart().clear();
        return CartMapper.toDto(customer.getCart());
    }

    private Customer findCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(
                        "Customer with id " + customerId + " not found"
                ));
    }

    private Product findProduct(Long productId, ProductType productType) {
        return switch (productType) {
            case COMPUTER -> computerRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(
                            "Computer with id " + productId + " not found"
                    ));
            case SMARTPHONE -> smartphoneRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(
                            "Smartphone with id " + productId + " not found"
                    ));
            case ELECTRONICS -> electronicsRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(
                            "Electronics with id " + productId + " not found"
                    ));
            default -> throw new IllegalArgumentException("Unknown product type: " + productType);
        };
    }
}