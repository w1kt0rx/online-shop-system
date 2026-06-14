package order.facade;

import invoice.dto.InvoiceDto;
import order.model.OrderProcessingResult;
import order.service.AsyncOrderProcessor;
import order.service.ConcurrentOrderProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * OrderFacade should do nothing but delegate batch/async order operations to
 * ConcurrentOrderProcessor and AsyncOrderProcessor.
 */
@ExtendWith(MockitoExtension.class)
class OrderFacadeTest {

    @Mock
    ConcurrentOrderProcessor concurrentOrderProcessor;
    @Mock
    AsyncOrderProcessor asyncOrderProcessor;

    @InjectMocks
    OrderFacade facade;

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
        verifyNoInteractions(asyncOrderProcessor);
    }

    @Test
    void shouldDelegateProcessOrderAsyncToAsyncProcessor() {
        CompletableFuture<InvoiceDto> future = CompletableFuture.completedFuture(null);
        when(asyncOrderProcessor.processOrderAsync(7L)).thenReturn(future);

        CompletableFuture<InvoiceDto> result = facade.processOrderAsync(7L);

        assertThat(result).isSameAs(future);
        verify(asyncOrderProcessor).processOrderAsync(7L);
        verifyNoInteractions(concurrentOrderProcessor);
    }

    @Test
    void shouldDelegateProcessBatchAsyncToAsyncProcessor() {
        List<Long> ids = List.of(1L, 2L);
        CompletableFuture<List<OrderProcessingResult>> future =
                CompletableFuture.completedFuture(List.of());
        when(asyncOrderProcessor.processBatchAsync(ids)).thenReturn(future);

        CompletableFuture<List<OrderProcessingResult>> result = facade.processBatchAsync(ids);

        assertThat(result).isSameAs(future);
        verify(asyncOrderProcessor).processBatchAsync(ids);
        verifyNoInteractions(concurrentOrderProcessor);
    }

    @Test
    void shouldDelegateShutdownToAsyncProcessor() {
        facade.shutdown();
        verify(asyncOrderProcessor).shutdown();
        verifyNoInteractions(concurrentOrderProcessor);
    }
}
