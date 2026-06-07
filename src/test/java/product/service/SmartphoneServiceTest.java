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
import product.dto.smartphone.CreateSmartphoneRequest;
import product.dto.smartphone.SmartphoneDto;
import product.dto.smartphone.UpdateSmartphoneRequest;
import product.model.smartphone.Smartphone;
import product.model.smartphone.configuration.*;
import product.repository.SmartphoneRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmartphoneServiceTest {

    @Mock
    SmartphoneRepository smartphoneRepository;
    @InjectMocks
    SmartphoneService smartphoneService;

    private Smartphone makePhone(Long id) {
        return new Smartphone(id, "iPhone", new BigDecimal("4000"), 10, new SmartphoneConfiguration());
    }

    @Test
    void shouldCreateSmartphone() {
        when(smartphoneRepository.getNextId()).thenReturn(1L);
        when(smartphoneRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SmartphoneDto result = smartphoneService.create(new CreateSmartphoneRequest(
                "iPhone 15", new BigDecimal("4000"), 10,
                Set.of(Accessory.CHARGER), BatteryCapacity.BATTERY_5000, Color.GOLD));

        assertThat(result.name()).isEqualTo("iPhone 15");
        verify(smartphoneRepository).save(any());
    }

    @ParameterizedTest
    @MethodSource("provideColors")
    void shouldCreateSmartphoneForAllColors(Color color) {
        when(smartphoneRepository.getNextId()).thenReturn(1L);
        when(smartphoneRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SmartphoneDto result = smartphoneService.create(new CreateSmartphoneRequest(
                "Phone", new BigDecimal("3000"), 5,
                Set.of(), BatteryCapacity.BATTERY_4000, color));

        assertThat(result).isNotNull();
    }

    private static Stream<Arguments> provideColors() {
        return Stream.of(Color.values()).map(Arguments::of);
    }

    @Test
    void shouldGetSmartphoneById() {
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(makePhone(1L)));

        SmartphoneDto result = smartphoneService.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void shouldThrowWhenSmartphoneNotFound() {
        when(smartphoneRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ProductNotFoundException.class)
                .isThrownBy(() -> smartphoneService.getById(99L))
                .withMessageContaining("99");
    }

    @Test
    void shouldReturnAllSmartphones() {
        when(smartphoneRepository.getAll()).thenReturn(List.of(makePhone(1L), makePhone(2L)));

        assertThat(smartphoneService.getAll()).hasSize(2);
    }

    @Test
    void shouldDeleteSmartphone() {
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(makePhone(1L)));

        smartphoneService.delete(1L);

        verify(smartphoneRepository).delete(1L);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentSmartphone() {
        when(smartphoneRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ProductNotFoundException.class)
                .isThrownBy(() -> smartphoneService.delete(99L));
        verify(smartphoneRepository, never()).delete(any());
    }

    @Test
    void shouldUpdateSmartphone() {
        Smartphone phone = makePhone(1L);
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(phone));
        when(smartphoneRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        SmartphoneDto result = smartphoneService.update(1L, new UpdateSmartphoneRequest(
                "Samsung S24", new BigDecimal("4200"), 8,
                Set.of(Accessory.PHONE_CASE), BatteryCapacity.BATTERY_6000, Color.WHITE));

        assertThat(result.name()).isEqualTo("Samsung S24");
        assertThat(result.quantity()).isEqualTo(8);
    }
}
