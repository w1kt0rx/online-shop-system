package product.facade;

import discount.dto.DiscountDto;
import discount.model.DiscountType;
import discount.service.DiscountService;
import exception.DiscountNotFoundException;
import order.service.ConcurrentOrderProcessor;
import order.model.OrderProcessingResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import product.dto.computer.ComputerDto;
import product.model.ProductType;
import product.service.ComputerService;
import product.service.ElectronicsService;
import product.service.SmartphoneService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductFacadeTest {

    @Mock
    ComputerService computerService;
    @Mock
    SmartphoneService smartphoneService;
    @Mock
    ElectronicsService electronicsService;
    @Mock
    DiscountService discountService;
    @Mock
    ConcurrentOrderProcessor concurrentOrderProcessor;

    @InjectMocks
    ProductFacade facade;


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
    }

    @Test
    void shouldDelegateGetActiveDiscountsToDiscountService() {
        when(discountService.getAllActive()).thenReturn(List.of());
        facade.getAllActiveDiscounts();
        verify(discountService).getAllActive();
    }

    @Test
    void shouldReturnDiscountDescription() {
        when(discountService.describeDiscount("CODE10")).thenReturn(Optional.of("10% off"));

        assertThat(facade.describeDiscount("CODE10")).contains("10% off");
    }

    @Test
    void shouldReturnEmptyWhenDiscountCodeUnknown() {
        when(discountService.describeDiscount("BAD")).thenReturn(Optional.empty());

        assertThat(facade.describeDiscount("BAD")).isEmpty();
    }

    @Test
    void shouldReturnDiscountedTotalOnValidCode() {
        when(discountService.applyDiscount("SAVE10", new BigDecimal("1000")))
                .thenReturn(new BigDecimal("900"));

        assertThat(facade.previewDiscountedTotal("SAVE10", new BigDecimal("1000")))
                .isEqualByComparingTo(new BigDecimal("900"));
    }

    @Test
    void shouldReturnOriginalTotalWhenDiscountCodeThrows() {
        when(discountService.applyDiscount("BAD", new BigDecimal("1000")))
                .thenThrow(new DiscountNotFoundException("not found"));

        assertThat(facade.previewDiscountedTotal("BAD", new BigDecimal("1000")))
                .isEqualByComparingTo(new BigDecimal("1000"));
    }


    @Test
    void shouldDelegateProcessBatchOrdersToConcurrentProcessor() {
        List<Long> ids = List.of(1L, 2L);
        when(concurrentOrderProcessor.processOrdersConcurrently(ids))
                .thenReturn(List.of(
                        OrderProcessingResult.failure(1L, "empty"),
                        OrderProcessingResult.failure(2L, "empty")));

        List<OrderProcessingResult> result = facade.processBatchOrders(ids);

        assertThat(result).hasSize(2);
        verify(concurrentOrderProcessor).processOrdersConcurrently(ids);
        verifyNoInteractions(computerService, smartphoneService, electronicsService);
    }

    @Test
    void shouldReturnActiveDiscountsList() {
        DiscountDto dto = new DiscountDto(1L, "TECH25", "25% off",
                DiscountType.PERCENTAGE, new BigDecimal("25"), BigDecimal.ZERO,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(30), true);
        when(discountService.getAllActive()).thenReturn(List.of(dto));

        List<DiscountDto> result = facade.getAllActiveDiscounts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("TECH25");
    }
}
