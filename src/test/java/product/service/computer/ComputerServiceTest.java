package product.service.computer;

import exception.InvalidProductException;
import exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.dto.ComputerDto;
import product.dto.CreateComputerRequest;
import product.dto.UpdateComputerRequest;
import product.model.computer.Computer;
import product.model.computer.configuration.*;
import product.repository.ComputerRepository;
import product.service.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComputerServiceTest {

    @Mock
    private ComputerRepository computerRepository;

    @InjectMocks
    private ProductService computerService;

    @Test
    void shouldCreateComputer() {

        CreateComputerRequest request =
                new CreateComputerRequest(
                        "Gaming PC",
                        BigDecimal.valueOf(5000),
                        10,
                        Processor.INTEL_I9,
                        Ram.RAM_32GB,
                        StorageType.SSD_2TB,
                        GraphicsCard.RTX_4070
                );

        when(computerRepository.getNextId())
                .thenReturn(1L);

        Computer savedComputer =
                new Computer(
                        1L,
                        "Gaming PC",
                        BigDecimal.valueOf(5000),
                        10,
                        new ComputerConfiguration()
                );

        when(computerRepository.save(any()))
                .thenReturn(savedComputer);

        ComputerDto result =
                computerService.createComputer(request);

        assertEquals(1L, result.id());
        assertEquals("Gaming PC", result.name());

        verify(computerRepository).save(any());
    }

    @Test
    void shouldFindComputerById() {

        Computer computer =
                new Computer(
                        1L,
                        "Gaming PC",
                        BigDecimal.valueOf(5000),
                        10,
                        new ComputerConfiguration()
                );

        when(computerRepository.findById(1L))
                .thenReturn(Optional.of(computer));

        ComputerDto result = computerService.getComputerById(1L);

        assertEquals(1L, result.id());
        assertEquals("Gaming PC", result.name());
    }

    @Test
    void shouldThrowExceptionWhenComputerNotFound() {

        when(computerRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> computerService.getComputerById(1L)
        );
    }

    @Test
    void shouldDeleteComputer() {

        Computer computer =
                new Computer(
                        1L,
                        "Gaming PC",
                        BigDecimal.valueOf(5000),
                        10,
                        new ComputerConfiguration()
                );

        when(computerRepository.findById(1L))
                .thenReturn(Optional.of(computer));

        computerService.deleteComputer(1L);

        verify(computerRepository)
                .delete(1L);
    }

    @Test
    void shouldThrowExceptionWhenDeletingNonExistingComputer() {

        when(computerRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> computerService.deleteComputer(1L)
        );
    }

    @Test
    void shouldReturnAllComputers() {

        List<Computer> computers =
                List.of(
                        new Computer(
                                1L,
                                "PC1",
                                BigDecimal.valueOf(3000),
                                5,
                                new ComputerConfiguration()
                        ),
                        new Computer(
                                2L,
                                "PC2",
                                BigDecimal.valueOf(4000),
                                5,
                                new ComputerConfiguration()
                        )
                );

        when(computerRepository.getAll())
                .thenReturn(computers);

        List<ComputerDto> result =
                computerService.getAllComputers();

        assertEquals(2, result.size());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistingComputer() {

        UpdateComputerRequest request =
                new UpdateComputerRequest(
                        "Gaming PC",
                        BigDecimal.valueOf(5000),
                        10,
                        Processor.INTEL_I9,
                        Ram.RAM_32GB,
                        StorageType.SSD_2TB,
                        GraphicsCard.RTX_4070
                );

        when(computerRepository.findById(1L))
                .thenReturn(Optional.empty());

        ProductNotFoundException exception =
                assertThrows(
                        ProductNotFoundException.class,
                        () -> computerService.updateComputer(
                                1L,
                                request
                        )
                );

        assertEquals(
                "Computer with id 1 not found",
                exception.getMessage()
        );

        verify(computerRepository, never())
                .save(any());
    }

    @Test
    void shouldUpdateMultipleFields() {

        Computer computer =
                new Computer(
                        1L,
                        "Old PC",
                        BigDecimal.valueOf(3000),
                        5,
                        new ComputerConfiguration()
                );

        when(computerRepository.findById(1L))
                .thenReturn(Optional.of(computer));

        when(computerRepository.save(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UpdateComputerRequest request =
                new UpdateComputerRequest(
                        "New PC",
                        BigDecimal.valueOf(7000),
                        20,
                        Processor.INTEL_I9,
                        Ram.RAM_64,
                        StorageType.SSD_2TB,
                        GraphicsCard.RTX_4070
                );

        ComputerDto result =
                computerService.updateComputer(
                        1L,
                        request
                );

        assertEquals("New PC", result.name());
        assertEquals(20, result.quantity());
    }

    @Test
    void shouldThrowExceptionWhenUpdateRequestIsInvalid() {
        Computer computer =  new Computer(1L,"comp",new BigDecimal("23"), 55, new ComputerConfiguration());
        when(computerRepository.findById(1L)).thenReturn(Optional.of(computer));

        UpdateComputerRequest request =
                new UpdateComputerRequest(
                        "",
                        BigDecimal.valueOf(-100),
                        -1,
                        null,
                        null,
                        null,
                        null
                );

        assertThrows(
                InvalidProductException.class,
                () -> computerService.updateComputer(
                        1L,
                        request
                )
        );

        verify(computerRepository)
                .findById(anyLong());

        verify(computerRepository, never())
                .save(any());
    }
}