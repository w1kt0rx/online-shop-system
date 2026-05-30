package product.service;

import exception.ProductNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import product.dto.*;
import product.model.computer.configuration.GraphicsCard;
import product.model.computer.configuration.Processor;
import product.model.computer.configuration.Ram;
import product.model.computer.configuration.StorageType;
import product.model.smartphone.configuration.Accessory;
import product.model.smartphone.configuration.BatteryCapacity;
import product.model.smartphone.configuration.Color;
import product.repository.impl.InMemoryComputerRepository;
import product.repository.impl.InMemoryElectronicsRepository;
import product.repository.impl.InMemorySmartphoneRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private ProductService productService;

    @BeforeEach
    void setup() {
        InMemoryComputerRepository computerRepository = new InMemoryComputerRepository();
        InMemorySmartphoneRepository smartphoneRepository = new InMemorySmartphoneRepository();
        InMemoryElectronicsRepository electronicsRepository = new InMemoryElectronicsRepository();

        productService = new ProductService(
                computerRepository,
                smartphoneRepository,
                electronicsRepository
        );
    }

    @ParameterizedTest
    @MethodSource("provideCreateComputerRequests")
    void shouldCreateComputerSuccessfully(CreateComputerRequest request) {
        // When
        ComputerDto result = productService.createComputer(request);

        // Then
        assertNotNull(result);
        assertNotNull(result.id());
        assertEquals(request.name(), result.name());
        assertEquals(request.basePrice(), result.basePrice());
        assertEquals(request.quantity(), result.quantity());
    }

    @ParameterizedTest
    @MethodSource("provideUpdateComputerRequests")
    void shouldUpdateComputerSuccessfully(UpdateComputerRequest updateRequest) {
        // Given
        CreateComputerRequest initialRequest = new CreateComputerRequest(
                "Computer", BigDecimal.valueOf(2000), 1,
                Processor.INTEL_I5, Ram.RAM_8GB, StorageType.SSD_512GB, GraphicsCard.INTEGRATED
        );
        ComputerDto created = productService.createComputer(initialRequest);
        // When
        ComputerDto updated = productService.updateComputer(created.id(), updateRequest);
        // Then
        assertEquals(created.id(), updated.id());
        assertEquals(updateRequest.name(), updated.name());
        assertEquals(updateRequest.basePrice(), updated.basePrice());
        assertEquals(updateRequest.quantity(), updated.quantity());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentComputer() {
        // Given
        Long nonExistentId = 99L;
        UpdateComputerRequest request = new UpdateComputerRequest(
                "Laptop", BigDecimal.valueOf(3000), 5,
                Processor.INTEL_I5, Ram.RAM_8GB, StorageType.SSD_512GB, GraphicsCard.INTEGRATED
        );

        // When & Then
        assertThrows(ProductNotFoundException.class, () ->
                productService.updateComputer(nonExistentId, request)
        );
    }

    @ParameterizedTest
    @MethodSource("provideCreateSmartphoneRequests")
    void shouldCreateSmartphoneSuccessfully(CreateSmartphoneRequest request) {
        // When
        SmartphoneDto result = productService.createSmartphone(request);

        // Then
        assertNotNull(result);
        assertEquals(request.name(), result.name());
        assertEquals(request.basePrice(), result.basePrice());
    }

    @ParameterizedTest
    @MethodSource("provideUpdateSmartphoneRequests")
    void shouldUpdateSmartphoneSuccessfully(UpdateSmartphoneRequest updateRequest) {
        // Given
        CreateSmartphoneRequest initialRequest = new CreateSmartphoneRequest(
                "Telefon", BigDecimal.valueOf(1000), 2,
                Set.of(), BatteryCapacity.BATTERY_6000, Color.BLACK
        );
        SmartphoneDto created = productService.createSmartphone(initialRequest);

        // When
        SmartphoneDto updated = productService.updateSmartphone(created.id(), updateRequest);

        // Then
        assertEquals(created.id(), updated.id());
        assertEquals(updateRequest.name(), updated.name());
        assertEquals(updateRequest.basePrice(), updated.basePrice());
    }

    @ParameterizedTest
    @MethodSource("provideCreateElectronicsRequests")
    void shouldCreateElectronicsSuccessfully(CreateElectronicsRequest request) {
        // When
        ElectronicsDto result = productService.createElectronics(request);

        // Then
        assertNotNull(result);
        assertEquals(request.name(), result.name());
    }

    @ParameterizedTest
    @MethodSource("provideUpdateElectronicsRequests")
    void shouldUpdateElectronicsSuccessfully(UpdateElectronicsRequest updateRequest) {
        // Given
        CreateElectronicsRequest initialRequest = new CreateElectronicsRequest("Radio", BigDecimal.valueOf(150), 10);
        ElectronicsDto created = productService.createElectronics(initialRequest);

        // When
        ElectronicsDto updated = productService.updateElectronics(created.id(), updateRequest);

        // Then
        assertEquals(created.id(), updated.id());
        assertEquals(updateRequest.name(), updated.name());
        assertEquals(updateRequest.basePrice(), updated.basePrice());
    }

    private static Stream<Arguments> provideCreateComputerRequests() {
        return Stream.of(
                Arguments.of(new CreateComputerRequest("Gaming PC", BigDecimal.valueOf(5000), 2, Processor.INTEL_I5, Ram.RAM_8GB, StorageType.SSD_512GB, GraphicsCard.INTEGRATED)),
                Arguments.of(new CreateComputerRequest("Office Laptop", BigDecimal.valueOf(2500), 10, Processor.INTEL_I5, Ram.RAM_8GB, StorageType.SSD_512GB, GraphicsCard.INTEGRATED))
        );
    }

    private static Stream<Arguments> provideUpdateComputerRequests() {
        return Stream.of(
                Arguments.of(new UpdateComputerRequest("PC", BigDecimal.valueOf(5500), 1, Processor.INTEL_I5, Ram.RAM_8GB, StorageType.SSD_512GB, GraphicsCard.INTEGRATED)),
                Arguments.of(new UpdateComputerRequest("Laptop", BigDecimal.valueOf(2400), 8, Processor.INTEL_I5, Ram.RAM_8GB, StorageType.SSD_512GB, GraphicsCard.INTEGRATED))
        );
    }

    private static Stream<Arguments> provideCreateSmartphoneRequests() {
        return Stream.of(
                Arguments.of(new CreateSmartphoneRequest("iPhone 15", BigDecimal.valueOf(4500), 5, Set.of(Accessory.CHARGER), BatteryCapacity.BATTERY_4000, Color.BLACK)),
                Arguments.of(new CreateSmartphoneRequest("Samsung S24", BigDecimal.valueOf(4200), 3, Set.of(), BatteryCapacity.BATTERY_4000, Color.BLACK))
        );
    }

    private static Stream<Arguments> provideUpdateSmartphoneRequests() {
        return Stream.of(
                Arguments.of(new UpdateSmartphoneRequest("iPhone 15 Pro", BigDecimal.valueOf(5500), 2, Set.of(Accessory.CHARGER, Accessory.HEADPHONES), BatteryCapacity.BATTERY_4000, Color.BLACK)),
                Arguments.of(new UpdateSmartphoneRequest("Samsung S24 Ultra", BigDecimal.valueOf(5000), 1, Set.of(), BatteryCapacity.BATTERY_4000, Color.BLACK))
        );
    }

    private static Stream<Arguments> provideCreateElectronicsRequests() {
        return Stream.of(
                Arguments.of(new CreateElectronicsRequest("Tv OLED", BigDecimal.valueOf(6000), 1)),
                Arguments.of(new CreateElectronicsRequest("Blender", BigDecimal.valueOf(300), 20))
        );
    }

    private static Stream<Arguments> provideUpdateElectronicsRequests() {
        return Stream.of(
                Arguments.of(new UpdateElectronicsRequest("Tv OLED 55'", BigDecimal.valueOf(5800), 2)),
                Arguments.of(new UpdateElectronicsRequest("Blender Kielichowy", BigDecimal.valueOf(350), 15))
        );
    }
}