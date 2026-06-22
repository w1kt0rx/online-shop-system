package cart.service;

import cart.dto.CartDto;
import cart.mapper.CartMapper;
import customer.model.Customer;
import customer.repository.CustomerRepository;
import exception.CustomerNotFoundException;
import exception.InsufficientStockException;
import exception.ProductNotFoundException;
import lombok.RequiredArgsConstructor;
import product.model.Product;
import product.model.ProductType;
import product.repository.ComputerRepository;
import product.repository.ElectronicsRepository;
import product.repository.SmartphoneRepository;

/**
 * Application service for managing a customer's shopping cart.
 * Provides add, remove, view, and clear operations. Stock availability is
 * checked on add; no stock is actually reserved at this stage — that happens
 * during order processing.
 */
@RequiredArgsConstructor
public class CartService {

    private final CustomerRepository customerRepository;
    private final ComputerRepository computerRepository;
    private final SmartphoneRepository smartphoneRepository;
    private final ElectronicsRepository electronicsRepository;

    /**
     * Adds a product to the customer's cart after verifying stock availability.
     *
     * @param customerId  identifier of the customer
     * @param productId   identifier of the product to add
     * @param productType category used to look up the product
     * @param quantity    number of units to add; must be positive
     * @return updated cart as a DTO
     * @throws CustomerNotFoundException  if the customer does not exist
     * @throws ProductNotFoundException   if the product does not exist
     * @throws InsufficientStockException if the requested quantity exceeds available stock
     */
    public CartDto addProduct(Long customerId, Long productId, ProductType productType, Integer quantity) {
        Customer customer = findCustomer(customerId);
        Product product = findProduct(productId, productType);

        if (quantity > product.getQuantity()) {
            throw new InsufficientStockException(
                    "Requested quantity " + quantity + " exceeds available stock " + product.getQuantity()
            );
        }

        customer.getCart().addProduct(product, quantity);
        return CartMapper.toDto(customer.getCart());
    }

    /**
     * Removes a product from the customer's cart.
     *
     * @param customerId  identifier of the customer
     * @param productId   identifier of the product to remove
     * @param productType category used to look up the product
     * @return updated cart as a DTO
     * @throws CustomerNotFoundException if the customer does not exist
     * @throws ProductNotFoundException  if the product does not exist
     */
    public CartDto removeProduct(Long customerId, Long productId, ProductType productType) {
        Customer customer = findCustomer(customerId);
        Product product = findProduct(productId, productType);

        customer.getCart().removeProduct(product);
        return CartMapper.toDto(customer.getCart());
    }

    /**
     * Returns the current contents of the customer's cart.
     *
     * @param customerId identifier of the customer
     * @return cart DTO; may be empty but never null
     * @throws CustomerNotFoundException if the customer does not exist
     */
    public CartDto getCart(Long customerId) {
        Customer customer = findCustomer(customerId);
        return CartMapper.toDto(customer.getCart());
    }

    /**
     * Removes all items from the customer's cart.
     *
     * @param customerId identifier of the customer
     * @return empty cart DTO
     * @throws CustomerNotFoundException if the customer does not exist
     */
    public CartDto clearCart(Long customerId) {
        Customer customer = findCustomer(customerId);
        customer.getCart().clear();
        return CartMapper.toDto(customer.getCart());
    }

    private Customer findCustomer(Long customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
    }

    private Product findProduct(Long productId, ProductType productType) {
        return switch (productType) {
            case COMPUTER -> computerRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productType, productId));

            case SMARTPHONE -> smartphoneRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productType, productId));

            case ELECTRONICS -> electronicsRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productType, productId));

            default -> throw new IllegalArgumentException("Unknown product type: " + productType);
        };
    }
}