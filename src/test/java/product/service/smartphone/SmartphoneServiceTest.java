package product.service.smartphone;

import exception.InvalidProductException;
import exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.dto.CreateSmartphoneRequest;
import product.dto.SmartphoneDto;
import product.dto.UpdateSmartphoneRequest;
import product.model.smartphone.Smartphone;
import product.model.smartphone.configuration.Accessory;
import product.model.smartphone.configuration.BatteryCapacity;
import product.model.smartphone.configuration.Color;
import product.model.smartphone.configuration.SmartphoneConfiguration;
import product.repository.SmartphoneRepository;
import product.service.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmartphoneServiceTest {

    @Mock
    private SmartphoneRepository smartphoneRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void shouldCreateSmartphone() {
        CreateSmartphoneRequest request = new CreateSmartphoneRequest(
                "iPhone 15",
                BigDecimal.valueOf(4500),
                10,
                Set.of(Accessory.CHARGER),
                BatteryCapacity.BATTERY_4000,
                Color.BLACK
        );

        when(smartphoneRepository.getNextId()).thenReturn(1L);

        Smartphone savedSmartphone = new Smartphone(
                1L, "iPhone 15", BigDecimal.valueOf(4500), 10, new SmartphoneConfiguration()
        );
        when(smartphoneRepository.save(any())).thenReturn(savedSmartphone);

        SmartphoneDto result = productService.createSmartphone(request);

        assertEquals(1L, result.id());
        assertEquals("iPhone 15", result.name());
        verify(smartphoneRepository).save(any());
    }

    @Test
    void shouldFindSmartphoneById() {
        Smartphone smartphone = new Smartphone(
                1L, "Samsung S24", BigDecimal.valueOf(4000), 5, new SmartphoneConfiguration()
        );
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(smartphone));

        SmartphoneDto result = productService.getSmartphoneById(1L);

        assertEquals(1L, result.id());
        assertEquals("Samsung S24", result.name());
    }

    @Test
    void shouldThrowExceptionWhenSmartphoneNotFound() {
        when(smartphoneRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getSmartphoneById(99L)
        );
    }

    @Test
    void shouldDeleteSmartphone() {
        Smartphone smartphone = new Smartphone(
                1L, "iPhone 15", BigDecimal.valueOf(4500), 10, new SmartphoneConfiguration()
        );
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(smartphone));

        productService.deleteSmartphone(1L);

        verify(smartphoneRepository).delete(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingSmartphone() {
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.deleteSmartphone(1L)
        );
    }

    @Test
    void shouldReturnAllSmartphones() {
        List<Smartphone> smartphones = List.of(
                new Smartphone(1L, "iPhone 15", BigDecimal.valueOf(4500), 5, new SmartphoneConfiguration()),
                new Smartphone(2L, "Samsung S24", BigDecimal.valueOf(4000), 3, new SmartphoneConfiguration())
        );
        when(smartphoneRepository.getAll()).thenReturn(smartphones);

        List<SmartphoneDto> result = productService.getAllSmartphones();

        assertEquals(2, result.size());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingSmartphone() {
        UpdateSmartphoneRequest request = new UpdateSmartphoneRequest(
                "iPhone 15 Pro",
                BigDecimal.valueOf(5500),
                3,
                Set.of(),
                BatteryCapacity.BATTERY_5000,
                Color.GOLD
        );
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateSmartphone(1L, request)
        );

        assertEquals("Smartphone with id 1 not found", exception.getMessage());
        verify(smartphoneRepository, never()).save(any());
    }

    @Test
    void shouldUpdateSmartphoneFields() {
        Smartphone smartphone = new Smartphone(
                1L, "iPhone 14", BigDecimal.valueOf(3500), 8, new SmartphoneConfiguration()
        );
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(smartphone));
        when(smartphoneRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateSmartphoneRequest request = new UpdateSmartphoneRequest(
                "iPhone 15",
                BigDecimal.valueOf(4500),
                5,
                Set.of(Accessory.CHARGER, Accessory.PHONE_CASE),
                BatteryCapacity.BATTERY_5000,
                Color.GOLD
        );

        SmartphoneDto result = productService.updateSmartphone(1L, request);

        assertEquals("iPhone 15", result.name());
        assertEquals(5, result.quantity());
    }

    @Test
    void shouldThrowExceptionWhenUpdateRequestIsInvalid() {
        Smartphone smartphone = new Smartphone(1L, "Phone", BigDecimal.valueOf(1000), 5, new SmartphoneConfiguration());
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(smartphone));

        UpdateSmartphoneRequest request = new UpdateSmartphoneRequest(
                "",
                BigDecimal.valueOf(-100),
                -1,
                Set.of(),
                BatteryCapacity.BATTERY_4000,
                Color.BLACK
        );

        assertThrows(
                InvalidProductException.class,
                () -> productService.updateSmartphone(1L, request)
        );

        verify(smartphoneRepository).findById(anyLong());
        verify(smartphoneRepository, never()).save(any());
    }
}
