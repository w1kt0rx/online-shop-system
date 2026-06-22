package product.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.dto.computer.ComputerDto;
import product.model.ProductType;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ProductFacade should do nothing but delegate to ComputerService,
 * SmartphoneService, and ElectronicsService — one call in, one call out.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    ComputerService computerService;
    @Mock
    SmartphoneService smartphoneService;
    @Mock
    ElectronicsService electronicsService;

    @InjectMocks
    ProductService facade;


    @Test
    void shouldDelegateGetAllComputersToComputerService() {
        when(computerService.getAll()).thenReturn(List.of());
        facade.getAllComputers();
        verify(computerService).getAll();
        verifyNoInteractions(smartphoneService, electronicsService);
    }

    @Test
    void shouldDelegateGetAllSmartphonesToSmartphoneService() {
        when(smartphoneService.getAll()).thenReturn(List.of());
        facade.getAllSmartphones();
        verify(smartphoneService).getAll();
        verifyNoInteractions(computerService, electronicsService);
    }

    @Test
    void shouldDelegateGetAllElectronicsToElectronicsService() {
        when(electronicsService.getAll()).thenReturn(List.of());
        facade.getAllElectronics();
        verify(electronicsService).getAll();
        verifyNoInteractions(computerService, smartphoneService);
    }

    @Test
    void shouldDelegateDeleteComputerToComputerService() {
        facade.deleteComputer(1L);
        verify(computerService).delete(1L);
        verifyNoInteractions(smartphoneService, electronicsService);
    }

    @Test
    void shouldDelegateDeleteSmartphoneToSmartphoneService() {
        facade.deleteSmartphone(2L);
        verify(smartphoneService).delete(2L);
        verifyNoInteractions(computerService, electronicsService);
    }

    @Test
    void shouldDelegateDeleteElectronicsToElectronicsService() {
        facade.deleteElectronics(3L);
        verify(electronicsService).delete(3L);
        verifyNoInteractions(computerService, smartphoneService);
    }

    @Test
    void shouldDelegateGetComputerByIdToComputerService() {
        ComputerDto dto = new ComputerDto(1L, "Dell", BigDecimal.TEN, BigDecimal.TEN,
                5, ProductType.COMPUTER, null);
        when(computerService.getById(1L)).thenReturn(dto);

        assertThat(facade.getComputerById(1L).id()).isEqualTo(1L);
        verify(computerService).getById(1L);
        verifyNoInteractions(smartphoneService, electronicsService);
    }
}
