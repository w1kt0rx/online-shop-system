package order.service;

import exception.OrderProcessingException;
import invoice.dto.InvoiceDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import order.model.OrderProcessingResult;

/**
 * This class is responsible for asynchronous order processing using CompletableFuture.
 * <p>
 * It processes orders in a non-blocking way, allowing the application to continue
 * executing other tasks while order processing runs in the background.
 * <p>
 * It supports processing both single and multiple orders. Single order processing
 * returns a CompletableFuture containing an invoice (InvoiceDto), while batch processing
 * returns a CompletableFuture containing a list of OrderProcessingResult objects.
 * <p>
 * All operations are executed asynchronously and may complete successfully or exceptionally
 * with an OrderProcessingException in case of failure.
 * <p>
 * The class also manages an internal thread pool used for asynchronous execution
 * and provides a method to shut it down gracefully when the application stops.
 */
@RequiredArgsConstructor
public class AsyncOrderProcessor {

    private final OrderProcessor orderProcessor;
    private final ExecutorService executor;

    public AsyncOrderProcessor(OrderProcessor orderProcessor, int threadPoolSize) {
        this.orderProcessor = orderProcessor;
        this.executor = Executors.newFixedThreadPool(threadPoolSize);
    }

    public CompletableFuture<InvoiceDto> processOrderAsync(Long customerId) {
        return processOrderAsync(customerId, null);
    }

    /**
     * Processes a single order asynchronously with an optional discount code.
     * <p>
     * The call returns immediately. The result (invoice) can be accessed via
     * thenAccept, thenApply, or join on the returned future.
     *
     * @param customerId   the customer identifier
     * @param discountCode optional discount code; null or empty means no discount
     * @return a CompletableFuture<InvoiceDto> — completed on success,
     * or completed exceptionally with OrderProcessingException on failure
     */
    public CompletableFuture<InvoiceDto> processOrderAsync(Long customerId, String discountCode) {
        return CompletableFuture.supplyAsync(() -> orderProcessor.processOrder(customerId, discountCode), executor);
    }

    /**
     * Processes a list of orders asynchronously — all started in parallel.
     * <p>
     * Returns a single ode CompletableFuture completed when all
     * orders have been processed (successfully or not). A failure of one order
     * does not interrupt the others — each result is stored in OrderProcessingResult.
     *
     * @param customerIds the list of customer identifiers to process
     * @return a CompletableFuture<List<OrderProcessingResult>> with results
     * in the same order as the input list
     */
    public CompletableFuture<List<OrderProcessingResult>> processBatchAsync(List<Long> customerIds) {
        List<CompletableFuture<OrderProcessingResult>> futures = customerIds
            .stream()
            .map(id ->
                CompletableFuture.supplyAsync(() -> orderProcessor.processOrder(id), executor).handle((invoice, ex) ->
                    ex == null
                        ? OrderProcessingResult.success(id, invoice)
                        : OrderProcessingResult.failure(
                            id,
                            ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage()
                        )
                )
            )
            .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenApply(v ->
            futures.stream().map(CompletableFuture::join).toList()
        );
    }

    /**
     * Shuts down the thread pool. Call this when the application is shutting down.
     * The calling thread is interrupted if waiting for termination is interrupted.
     */
    public void shutdown() {
        executor.shutdown();
    }
}
