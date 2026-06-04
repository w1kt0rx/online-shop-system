package order.service;

import exception.OrderProcessingException;
import invoice.dto.InvoiceDto;
import lombok.RequiredArgsConstructor;
import order.model.OrderProcessingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@RequiredArgsConstructor
public class ConcurrentOrderProcessor {

    private final OrderProcessor orderProcessor;
    private final int threadPoolSize;

    public List<OrderProcessingResult> processOrdersConcurrently(List<Long> customerIds) {
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<OrderProcessingResult>> futures = new ArrayList<>();

        for (Long customerId : customerIds) {
            futures.add(executor.submit(() -> processOne(customerId)));
        }

        executor.shutdown();
        try {
            if(!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                throw new OrderProcessingException("Concurrent order processing timed out after 30 seconds");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            throw new OrderProcessingException("Concurrent order processing was interupted", e);
        }

        List<OrderProcessingResult> results = new ArrayList<>();
        for (Future<OrderProcessingResult> future : futures) {
            try {
                results.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                results.add(OrderProcessingResult.failure(-1L, "Failed to retrieve result: " + e.getMessage()));
            }
        }
        return results;
    }

    private OrderProcessingResult processOne(Long customerId) {
        try {
            InvoiceDto invoice = orderProcessor.processOrder(customerId);
            return OrderProcessingResult.success(customerId, invoice);
        } catch (Exception e) {
            return OrderProcessingResult.failure(customerId, e.getMessage());
        }
    }
}
