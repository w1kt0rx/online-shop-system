package order.facade;

import invoice.dto.InvoiceDto;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import order.model.OrderProcessingResult;
import order.service.AsyncOrderProcessor;
import order.service.ConcurrentOrderProcessor;

/**
 * Facade that exposes batch and asynchronous order-processing operations
 * through a single entry point.
 * <p>
 * Callers that need to check out carts for many customers at once (e.g. an
 * end-of-day batch job or an admin tool) interact exclusively with this
 * class rather than calling ConcurrentOrderProcessor or
 * AsyncOrderProcessor directly.
 * </p>
 */
@RequiredArgsConstructor
public class OrderFacade {

    private final ConcurrentOrderProcessor concurrentOrderProcessor;
    private final AsyncOrderProcessor asyncOrderProcessor;

    /**
     * Processes orders for multiple customers in parallel.
     *
     * @param customerIds list of customer IDs to check out
     * @return per-customer results; failures are included as result entries rather
     * than causing the entire batch to fail
     * @see ConcurrentOrderProcessor#processOrdersConcurrently(List)
     */
    public List<OrderProcessingResult> processBatchOrders(List<Long> customerIds) {
        return concurrentOrderProcessor.processOrdersConcurrently(customerIds);
    }

    /**
     * Processes a single order asynchronously (without blocking the thread).
     * The result can be accessed using thenAccept, thenApply, or join.
     *
     * @param customerId the customer identifier
     * @return a CompletableFuture containing the invoice; completed exceptionally if an error occurs
     */
    public CompletableFuture<InvoiceDto> processOrderAsync(Long customerId) {
        return asyncOrderProcessor.processOrderAsync(customerId);
    }

    /**
     * Processes a single order asynchronously with an optional discount code,
     * without blocking the calling thread.
     *
     * @param customerId   the customer identifier
     * @param discountCode optional discount code; null or empty means no discount
     * @return a CompletableFuture containing the invoice; completed exceptionally if an error occurs
     */
    public CompletableFuture<InvoiceDto> processOrderAsync(Long customerId, String discountCode) {
        return asyncOrderProcessor.processOrderAsync(customerId, discountCode);
    }

    /**
     * Processes a list of orders asynchronously — all in parallel without blocking.
     * Completes when all orders have finished processing (successfully or exceptionally).
     *
     * @param customerIds the list of customer identifiers
     * @return a CompletableFuture containing the results in the original input order
     */
    public CompletableFuture<List<OrderProcessingResult>> processBatchAsync(List<Long> customerIds) {
        return asyncOrderProcessor.processBatchAsync(customerIds);
    }

    /**
     * Shuts down the underlying async executor. Call this when the application is shutting down.
     */
    public void shutdown() {
        asyncOrderProcessor.shutdown();
    }
}
