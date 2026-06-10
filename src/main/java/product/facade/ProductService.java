package product.facade;

import discount.dto.DiscountDto;
import discount.service.DiscountService;
import exception.DiscountNotFoundException;
import exception.InvalidProductException;
import invoice.dto.InvoiceDto;
import lombok.RequiredArgsConstructor;
import order.service.AsyncOrderProcessor;
import order.service.ConcurrentOrderProcessor;
import order.model.OrderProcessingResult;
import product.dto.computer.ComputerDto;
import product.dto.computer.CreateComputerRequest;
import product.dto.computer.UpdateComputerRequest;
import product.dto.electronics.CreateElectronicsRequest;
import product.dto.electronics.ElectronicsDto;
import product.dto.electronics.UpdateElectronicsRequest;
import product.dto.smartphone.CreateSmartphoneRequest;
import product.dto.smartphone.SmartphoneDto;
import product.dto.smartphone.UpdateSmartphoneRequest;
import product.service.ComputerService;
import product.service.ElectronicsService;
import product.service.SmartphoneService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Facade that exposes all product, discount, and batch-order operations through a single entry point.
 * <p>
 * The CLI and integration layer interact exclusively with this class rather than calling
 * individual services directly. It delegates to ComputerService,
 * SmartphoneService, ElectronicsService, DiscountService, and
 * ConcurrentOrderProcessor.
 * </p>
 */
@RequiredArgsConstructor
public class ProductService {

    private final ComputerService computerService;
    private final SmartphoneService smartphoneService;
    private final ElectronicsService electronicsService;
    private final DiscountService discountService;
    private final ConcurrentOrderProcessor concurrentOrderProcessor;
    private final AsyncOrderProcessor asyncOrderProcessor;

    /** Creates a new computer product. */
    public ComputerDto createComputer(CreateComputerRequest request) {
        return computerService.create(request);
    }

    /** Updates an existing computer product. */
    public ComputerDto updateComputer(Long id, UpdateComputerRequest request) {
        return computerService.update(id, request);
    }

    /** Returns a computer by id. */
    public ComputerDto getComputerById(Long id) {
        return computerService.getById(id);
    }

    /** Returns all computers. */
    public List<ComputerDto> getAllComputers() {
        return computerService.getAll();
    }

    /** Deletes a computer by id. */
    public void deleteComputer(Long id) {
        computerService.delete(id);
    }

    /** Creates a new smartphone product. */
    public SmartphoneDto createSmartphone(CreateSmartphoneRequest request) {
        return smartphoneService.create(request);
    }

    /** Updates an existing smartphone product. */
    public SmartphoneDto updateSmartphone(Long id, UpdateSmartphoneRequest request) {
        return smartphoneService.update(id, request);
    }

    /** Returns a smartphone by id. */
    public SmartphoneDto getSmartphoneById(Long id) {
        return smartphoneService.getById(id);
    }

    /** Returns all smartphones. */
    public List<SmartphoneDto> getAllSmartphones() {
        return smartphoneService.getAll();
    }

    /** Deletes a smartphone by id. */
    public void deleteSmartphone(Long id) {
        smartphoneService.delete(id);
    }

    /** Creates a new electronics product. */
    public ElectronicsDto createElectronics(CreateElectronicsRequest request) {
        return electronicsService.create(request);
    }

    /** Updates an existing electronics product. */
    public ElectronicsDto updateElectronics(Long id, UpdateElectronicsRequest request) {
        return electronicsService.update(id, request);
    }

    /** Returns an electronics item by id. */
    public ElectronicsDto getElectronicsById(Long id) {
        return electronicsService.getById(id);
    }

    /** Returns all electronics items. */
    public List<ElectronicsDto> getAllElectronics() {
        return electronicsService.getAll();
    }

    /** Deletes an electronics item by id. */
    public void deleteElectronics(Long id) {
        electronicsService.delete(id);
    }

    /**
     * Returns all discounts that are currently active and within their validity window.
     *
     * @return list of active discount DTOs
     */
    public List<DiscountDto> getAllActiveDiscounts() {
        return discountService.getAllActive();
    }

    /**
     * Returns a human-readable description for a discount code, if valid.
     *
     * @param code the discount code to describe
     * @return description string wrapped in Optional, or empty if the code
     *         is unknown or the discount is expired/inactive
     */
    public Optional<String> describeDiscount(String code) {
        return discountService.describeDiscount(code);
    }

    /**
     * Previews the total after applying a discount code without committing any order.
     * If the code is invalid or does not meet minimum requirements, the original total
     * is returned unchanged.
     *
     * @param code  discount code to preview
     * @param total the hypothetical order total
     * @return the discounted total, or total if the code cannot be applied
     */
    public BigDecimal previewDiscountedTotal(String code, BigDecimal total) {
        try {
            return discountService.applyDiscount(code, total);
        } catch (DiscountNotFoundException | InvalidProductException e) {
            return total;
        }
    }

    /**
     * Processes orders for multiple customers in parallel.
     *
     * @param customerIds list of customer IDs to check out
     * @return per-customer results; failures are included as result entries rather
     *         than causing the entire batch to fail
     * @see ConcurrentOrderProcessor#processOrdersConcurrently(List)
     */
    public List<OrderProcessingResult> processBatchOrders(List<Long> customerIds) {
        return concurrentOrderProcessor.processOrdersConcurrently(customerIds);
    }

    /**
     * Processes a single order asynchronously (without blocking the thread).
     * The result can be accessed using thenAccept, thenApply, or join.
     *
     * @param customerId the customer identifier
     * @return a CompletableFuture containing the invoice; completed exceptionally if an error occurs
     */
    public CompletableFuture<InvoiceDto> processOrderAsync(Long customerId) {
        return asyncOrderProcessor.processOrderAsync(customerId);
    }

    /**
     * Processes a list of orders asynchronously — all in parallel without blocking.
     * Completes when all orders have finished processing (successfully or exceptionally).
     *
     * @param customerIds the list of customer identifiers
     * @return a CompletableFuture containing the results in the original input order
     */
    public CompletableFuture<List<OrderProcessingResult>> processBatchAsync(List<Long> customerIds) {
        return asyncOrderProcessor.processBatchAsync(customerIds);
    }
}
