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
import product.dto.ComputerDto;
import product.dto.CreateComputerRequest;
import product.dto.UpdateComputerRequest;
import product.model.computer.Computer;
import product.model.computer.configuration.*;
import product.repository.ComputerRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComputerServiceTest {

    @Mock ComputerRepository computerRepository;
    @InjectMocks ComputerService computerService;

    private Computer makeComputer(Long id) {
        return new Computer(id, "Dell XPS", new BigDecimal("3000"), 5, new ComputerConfiguration());
    }

    @Test
    void shouldCreateComputerAndSaveToRepository() {
        when(computerRepository.getNextId()).thenReturn(1L);
        when(computerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ComputerDto result = computerService.create(new CreateComputerRequest(
                "Dell XPS", new BigDecimal("3000"), 5,
                Processor.INTEL_I7, Ram.RAM_16GB, StorageType.SSD_1TB, GraphicsCard.RTX_3050));

        assertThat(result.name()).isEqualTo("Dell XPS");
        assertThat(result.basePrice()).isEqualByComparingTo(new BigDecimal("3000"));
        verify(computerRepository).save(any());
    }

    @ParameterizedTest
    @MethodSource("provideCreateRequests")
    void shouldCreateComputerForVariousConfigurations(
            String name, BigDecimal price, Processor proc, Ram ram) {
        when(computerRepository.getNextId()).thenReturn(1L);
        when(computerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ComputerDto result = computerService.create(new CreateComputerRequest(
                name, price, 1, proc, ram, StorageType.SSD_512GB, GraphicsCard.INTEGRATED));

        assertThat(result.name()).isEqualTo(name);
    }

    private static Stream<Arguments> provideCreateRequests() {
        return Stream.of(
                Arguments.of("Budget PC",  new BigDecimal("1500"), Processor.AMD_RYZEN_5, Ram.RAM_8GB),
                Arguments.of("Mid PC",     new BigDecimal("3000"), Processor.INTEL_I7,    Ram.RAM_16GB),
                Arguments.of("High-end PC",new BigDecimal("6000"), Processor.INTEL_I9,    Ram.RAM_32GB)
        );
    }

    @Test
    void shouldGetComputerById() {
        when(computerRepository.findById(1L)).thenReturn(Optional.of(makeComputer(1L)));

        ComputerDto result = computerService.getById(1L);

        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    void shouldThrowWhenComputerNotFound() {
        when(computerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ProductNotFoundException.class)
                .isThrownBy(() -> computerService.getById(99L))
                .withMessageContaining("99");
    }

    @Test
    void shouldReturnAllComputers() {
        when(computerRepository.getAll()).thenReturn(List.of(makeComputer(1L), makeComputer(2L)));

        assertThat(computerService.getAll()).hasSize(2);
    }

    @Test
    void shouldDeleteComputer() {
        when(computerRepository.findById(1L)).thenReturn(Optional.of(makeComputer(1L)));

        computerService.delete(1L);

        verify(computerRepository).delete(1L);
    }

    @Test
    void shouldThrowWhenDeletingNonExistentComputer() {
        when(computerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ProductNotFoundException.class)
                .isThrownBy(() -> computerService.delete(99L));
        verify(computerRepository, never()).delete(any());
    }

    @Test
    void shouldUpdateComputerFields() {
        Computer computer = makeComputer(1L);
        when(computerRepository.findById(1L)).thenReturn(Optional.of(computer));
        when(computerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ComputerDto result = computerService.update(1L, new UpdateComputerRequest(
                "Updated PC", new BigDecimal("4000"), 3,
                Processor.INTEL_I9, Ram.RAM_32GB, StorageType.SSD_2TB, GraphicsCard.RTX_4070));

        assertThat(result.name()).isEqualTo("Updated PC");
        assertThat(result.quantity()).isEqualTo(3);
    }

    @Test
    void shouldThrowWhenUpdatingNonExistentComputer() {
        when(computerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(ProductNotFoundException.class)
                .isThrownBy(() -> computerService.update(99L, new UpdateComputerRequest(
                        "X", BigDecimal.ONE, 1,
                        Processor.INTEL_I5, Ram.RAM_8GB, StorageType.SSD_512GB, GraphicsCard.INTEGRATED)));
        verify(computerRepository, never()).save(any());
    }
}
