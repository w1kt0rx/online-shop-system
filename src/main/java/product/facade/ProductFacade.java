package product.facade;

import discount.dto.DiscountDto;
import discount.service.DiscountService;
import exception.DiscountNotFoundException;
import exception.InvalidProductException;
import lombok.RequiredArgsConstructor;
import order.service.ConcurrentOrderProcessor;
import order.model.OrderProcessingResult;
import product.dto.*;
import product.service.ComputerService;
import product.service.ElectronicsService;
import product.service.SmartphoneService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ProductFacade {

    private final ComputerService computerService;
    private final SmartphoneService smartphoneService;
    private final ElectronicsService electronicsService;
    private final DiscountService discountService;
    private final ConcurrentOrderProcessor concurrentOrderProcessor;

    // ── Computers ─────────────────────────────────────────────────────

    public ComputerDto createComputer(CreateComputerRequest request) {
        return computerService.create(request);
    }

    public ComputerDto updateComputer(Long id, UpdateComputerRequest request) {
        return computerService.update(id, request);
    }

    public ComputerDto getComputerById(Long id) {
        return computerService.getById(id);
    }

    public List<ComputerDto> getAllComputers() {
        return computerService.getAll();
    }

    public void deleteComputer(Long id) {
        computerService.delete(id);
    }

    // ── Smartphones ───────────────────────────────────────────────────

    public SmartphoneDto createSmartphone(CreateSmartphoneRequest request) {
        return smartphoneService.create(request);
    }

    public SmartphoneDto updateSmartphone(Long id, UpdateSmartphoneRequest request) {
        return smartphoneService.update(id, request);
    }

    public SmartphoneDto getSmartphoneById(Long id) {
        return smartphoneService.getById(id);
    }

    public List<SmartphoneDto> getAllSmartphones() {
        return smartphoneService.getAll();
    }

    public void deleteSmartphone(Long id) {
        smartphoneService.delete(id);
    }

    // ── Electronics ───────────────────────────────────────────────────

    public ElectronicsDto createElectronics(CreateElectronicsRequest request) {
        return electronicsService.create(request);
    }

    public ElectronicsDto updateElectronics(Long id, UpdateElectronicsRequest request) {
        return electronicsService.update(id, request);
    }

    public ElectronicsDto getElectronicsById(Long id) {
        return electronicsService.getById(id);
    }

    public List<ElectronicsDto> getAllElectronics() {
        return electronicsService.getAll();
    }

    public void deleteElectronics(Long id) {
        electronicsService.delete(id);
    }

    // ── Discounts ─────────────────────────────────────────────────────

    public List<DiscountDto> getAllActiveDiscounts() {
        return discountService.getAllActive();
    }

    public Optional<String> describeDiscount(String code) {
        return discountService.describeDiscount(code);
    }

    public BigDecimal previewDiscountedTotal(String code, BigDecimal total) {
        try {
            return discountService.applyDiscount(code, total);
        } catch (DiscountNotFoundException | InvalidProductException e) {
            return total;
        }
    }

    // ── Task 11: Concurrent batch processing ──────────────────────────

    public List<OrderProcessingResult> processBatchOrders(List<Long> customerIds) {
        return concurrentOrderProcessor.processOrdersConcurrently(customerIds);
    }
}
