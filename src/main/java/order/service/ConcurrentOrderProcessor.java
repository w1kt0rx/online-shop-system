package order.service;

import exception.OrderProcessingException;
import invoice.dto.InvoiceDto;
import lombok.RequiredArgsConstructor;
import order.model.OrderProcessingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Processes multiple orders in parallel using a fixed-size thread pool.
 * <p>
 * Wraps OrderProcessor to enable batch order processing. Each order
 * is submitted as an independent task; results (both successes and failures) are
 * collected into an OrderProcessingResult list so callers can inspect
 * per-order outcomes without a single failure aborting the entire batch.
 * </p>
 * <p>
 * The executor shuts down after all tasks are submitted and waits up to
 * 30 seconds for completion. A timeout or thread interruption throws
 * OrderProcessingException.
 * </p>
 */
@RequiredArgsConstructor
public class ConcurrentOrderProcessor {

    private final OrderProcessor orderProcessor;
    private final int threadPoolSize;

    /**
     * Processes orders for all given customer IDs concurrently.
     * <p>
     * Each customer ID is processed in a separate thread. Failed orders are
     * recorded as OrderProcessingResult#failure entries rather than
     * propagating the exception, ensuring all results are always returned.
     * </p>
     *
     * @param customerIds list of customer IDs whose carts should be checked out
     * @return list of results in submission order; never null
     * @throws OrderProcessingException if the thread pool times out or is interrupted
     */
    public List<OrderProcessingResult> processOrdersConcurrently(List<Long> customerIds) {
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
        List<Future<OrderProcessingResult>> futures = new ArrayList<>();

        for (Long customerId : customerIds) {
            futures.add(executor.submit(() -> processOne(customerId)));
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
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

    /**
     * Processes a single order and wraps any exception in a failure result.
     *
     * @param customerId identifier of the customer to process
     * @return success result with the invoice, or failure result with an error message
     */
    private OrderProcessingResult processOne(Long customerId) {
        try {
            InvoiceDto invoice = orderProcessor.processOrder(customerId);
            return OrderProcessingResult.success(customerId, invoice);
        } catch (Exception e) {
            return OrderProcessingResult.failure(customerId, e.getMessage());
        }
    }
}
