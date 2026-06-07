package product.service;

import exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.dto.CreateElectronicsRequest;
import product.dto.ElectronicsDto;
import product.dto.UpdateElectronicsRequest;
import product.model.electronics.Electronics;
import product.repository.ElectronicsRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ElectronicsServiceTest {

    @Mock ElectronicsRepository electronicsRepository;
    @InjectMocks ElectronicsService electronicsService;

    private Electronics makeProduct(Long id) {
        return new Electronics(id, "Monitor", new BigDecimal("800"), 10);
    }

    @ParameterizedTest
    @MethodSource("provideElectronics")
    void shouldCreateElectronicsForVariousProducts(String name, BigDecimal price, int qty) {
        when(electronicsRepository.getNextId()).thenReturn(1L);
        when(electronicsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ElectronicsDto result = electronicsService.create(
                new CreateElectronicsRequest(name, price, qty));

        assertThat(result.name()).isEqualTo(name);
        assertThat(result.basePrice()).isEqualByComparingTo(price);
        assertThat(result.quantity()).isEqualTo(qty);
    }

    private static Stream<Arguments> provideElectronics() {
        return Stream.of(
                Arguments.of("Monitor 4K",  new BigDecimal("1500"), 12),
                Arguments.of("Keyboard",    new BigDecimal("350"),  40),
                Arguments.of("Headphones",  new BigDecimal("800"),  20),
                Arguments.of("Webcam",      new BigDecimal("250"),  30)
        );
    }

    @Test
    void shouldGetElectronicsById() {
        when(electronicsRepository.findById(1L)).thenReturn(Optional.of(makeProduct(1L)));

        ElectronicsDto result = electronicsService.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Monitor");
    }

    @Test
    void shouldThrowWhenElectronicsNotFound() {
        when(electronicsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ProductNotFoundException.class)
                .isThrownBy(() -> electronicsService.getById(99L))
                .withMessageContaining("99");
    }

    @Test
    void shouldReturnAllElectronics() {
        when(electronicsRepository.getAll()).thenReturn(
                List.of(makeProduct(1L), makeProduct(2L), makeProduct(3L)));

        assertThat(electronicsService.getAll()).hasSize(3);
    }

    @Test
    void shouldDeleteElectronics() {
        when(electronicsRepository.findById(1L)).thenReturn(Optional.of(makeProduct(1L)));

        electronicsService.delete(1L);

        verify(electronicsRepository).delete(1L);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentElectronics() {
        when(electronicsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ProductNotFoundException.class)
                .isThrownBy(() -> electronicsService.delete(99L));
        verify(electronicsRepository, never()).delete(any());
    }

    @Test
    void shouldUpdateElectronics() {
        Electronics product = makeProduct(1L);
        when(electronicsRepository.findById(1L)).thenReturn(Optional.of(product));
        when(electronicsRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ElectronicsDto result = electronicsService.update(1L, new UpdateElectronicsRequest(
                "Monitor 8K", new BigDecimal("3000"), 5));

        assertThat(result.name()).isEqualTo("Monitor 8K");
        assertThat(result.basePrice()).isEqualByComparingTo(new BigDecimal("3000"));
        assertThat(result.quantity()).isEqualTo(5);
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentElectronics() {
        when(electronicsRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ProductNotFoundException.class)
                .isThrownBy(() -> electronicsService.update(99L,
                        new UpdateElectronicsRequest("X", BigDecimal.ONE, 1)));
        verify(electronicsRepository, never()).save(any());
    }
}
