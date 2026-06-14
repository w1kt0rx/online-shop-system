package product.facade;

import lombok.RequiredArgsConstructor;
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

import java.util.List;

/**
 * Facade that exposes all product-catalog operations through a single entry point.
 * <p>
 * The CLI and integration layer interact exclusively with this class rather than calling
 * ComputerService, SmartphoneService, or ElectronicsService
 * directly. It delegates strictly to those three product services — discounts, carts,
 * orders, customers, and invoices are out of scope for this facade and are accessed
 * through their own dedicated services/facades.
 * </p>
 */
@RequiredArgsConstructor
public class ProductFacade {

    private final ComputerService computerService;
    private final SmartphoneService smartphoneService;
    private final ElectronicsService electronicsService;

    /**
     * Creates a new computer product.
     */
    public ComputerDto createComputer(CreateComputerRequest request) {
        return computerService.create(request);
    }

    /**
     * Updates an existing computer product.
     */
    public ComputerDto updateComputer(Long id, UpdateComputerRequest request) {
        return computerService.update(id, request);
    }

    /**
     * Returns a computer by id.
     */
    public ComputerDto getComputerById(Long id) {
        return computerService.getById(id);
    }

    /**
     * Returns all computers.
     */
    public List<ComputerDto> getAllComputers() {
        return computerService.getAll();
    }

    /**
     * Deletes a computer by id.
     */
    public void deleteComputer(Long id) {
        computerService.delete(id);
    }

    /**
     * Creates a new smartphone product.
     */
    public SmartphoneDto createSmartphone(CreateSmartphoneRequest request) {
        return smartphoneService.create(request);
    }

    /**
     * Updates an existing smartphone product.
     */
    public SmartphoneDto updateSmartphone(Long id, UpdateSmartphoneRequest request) {
        return smartphoneService.update(id, request);
    }

    /**
     * Returns a smartphone by id.
     */
    public SmartphoneDto getSmartphoneById(Long id) {
        return smartphoneService.getById(id);
    }

    /**
     * Returns all smartphones.
     */
    public List<SmartphoneDto> getAllSmartphones() {
        return smartphoneService.getAll();
    }

    /**
     * Deletes a smartphone by id.
     */
    public void deleteSmartphone(Long id) {
        smartphoneService.delete(id);
    }

    /**
     * Creates a new electronics product.
     */
    public ElectronicsDto createElectronics(CreateElectronicsRequest request) {
        return electronicsService.create(request);
    }

    /**
     * Updates an existing electronics product.
     */
    public ElectronicsDto updateElectronics(Long id, UpdateElectronicsRequest request) {
        return electronicsService.update(id, request);
    }

    /**
     * Returns an electronics item by id.
     */
    public ElectronicsDto getElectronicsById(Long id) {
        return electronicsService.getById(id);
    }

    /**
     * Returns all electronics items.
     */
    public List<ElectronicsDto> getAllElectronics() {
        return electronicsService.getAll();
    }

    /**
     * Deletes an electronics item by id.
     */
    public void deleteElectronics(Long id) {
        electronicsService.delete(id);
    }
}
