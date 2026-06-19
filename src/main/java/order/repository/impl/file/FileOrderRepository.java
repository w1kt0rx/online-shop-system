package order.repository.impl.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import order.model.Order;
import order.model.OrderStatus;
import order.repository.OrderRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * File-backed OrderRepository that persists orders as JSON snapshots.
 * <p>
 * Uses an in-memory ConcurrentHashMap as a read cache and writes a JSON
 * array of OrderSnapshot records to disk on every mutation. On startup,
 * existing snapshots are loaded from the file and the ID sequence is fast-forwarded
 * past the highest persisted id to avoid collisions.
 * </p>
 */
public class FileOrderRepository implements OrderRepository {

    private final File file;
    private final ObjectMapper mapper;
    private final Map<Long, Order> cache = new ConcurrentHashMap<>();
    private final List<OrderSnapshot> persistedSnapshots = new ArrayList<>();
    private final AtomicLong idSequence = new AtomicLong(1L);

    /**
     * Constructs the repository and loads any previously persisted snapshots from disk.
     *
     * @param filePath path to the JSON file used for persistence; the file is created
     *                 automatically on the first write if it does not yet exist
     */
    public FileOrderRepository(String filePath) {
        this.file = new File(filePath);
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        loadFromFile();
    }

    /**
     * Saves the order to the cache and persists all snapshots to disk.
     */
    @Override
    public Order save(Order entity) {
        cache.put(entity.getId(), entity);
        persistToFile();
        return entity;
    }

    /**
     * Removes the order from both the cache and the persisted snapshot list.
     */
    @Override
    public void delete(Long id) {
        cache.remove(id);
        persistedSnapshots.removeIf(s -> s.id().equals(id));
        persistToFile();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(cache.get(id));
    }

    /**
     * Returns an unmodifiable view of all cached orders.
     */
    @Override
    public List<Order> getAll() {
        return List.copyOf(cache.values());
    }

    @Override
    public Long getNextId() {
        return idSequence.getAndIncrement();
    }

    /**
     * Returns an unmodifiable copy of all snapshots that have been written to the file.
     *
     * @return list of persisted OrderSnapshot records
     */
    public List<OrderSnapshot> getPersistedSnapshots() {
        return List.copyOf(persistedSnapshots);
    }

    /**
     * Returns the number of snapshots currently persisted on disk.
     *
     * @return count of persisted snapshots
     */
    public int getPersistedCount() {
        return persistedSnapshots.size();
    }

    /**
     * Reads the JSON file, populates #persistedSnapshots, reconstructs each
     * snapshot into an Order in the cache (via Order#restore, so they are
     * immediately visible through findById/getAll), and advances the
     * ID sequence past the highest persisted id. Silently continues if the file
     * does not exist; logs to System.err on a parse error.
     * <p>
     * Note: reconstructed orders have an empty item list, since cart item details
     * are not part of the persisted snapshot.
     * </p>
     */
    private void loadFromFile() {
        if (!file.exists() || file.length() == 0) return;
        try {
            List<OrderSnapshot> loaded = mapper.readValue(
                    file, new TypeReference<>() {
                    });
            persistedSnapshots.addAll(loaded);
            loaded.forEach(s -> {
                if (s.id() >= idSequence.get()) idSequence.set(s.id() + 1);
                cache.put(s.id(), Order.restore(
                        s.id(),
                        s.customerId(),
                        s.totalPrice(),
                        s.createdAt(),
                        s.updatedAt(),
                        s.confirmedAt(),
                        OrderStatus.valueOf(s.status())));
            });
            System.out.printf("Loaded %d snapshots from %s%n",
                    loaded.size(), file.getPath());
        } catch (IOException e) {
            System.err.println("Could not load: " + e.getMessage());
        }
    }

    /**
     * Merges cache entries with existing persisted snapshots and writes the combined
     * list to disk as pretty-printed JSON. Logs to System.err on write failure.
     * <p>
     * Snapshots are upserted by order id: every order currently in the cache gets a
     * freshly built snapshot reflecting its current state (overwriting any older
     * snapshot for the same id), while snapshots for orders not present in the cache
     * (e.g. loaded from a previous session) are preserved as-is.
     * </p>
     */
    private void persistToFile() {
        try {
            Map<Long, OrderSnapshot> snapshotsById = new LinkedHashMap<>();

            persistedSnapshots.forEach(snapshot -> snapshotsById.put(snapshot.id(), snapshot));

            cache.values().forEach(order -> snapshotsById.put(order.getId(), toSnapshot(order)));

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, new ArrayList<>(snapshotsById.values()));
        } catch (IOException e) {
            System.err.println("[FileOrderRepository] Could not persist: " + e.getMessage());
        }
    }

    /**
     * Builds an OrderSnapshot reflecting the current state of the given order.
     */
    private OrderSnapshot toSnapshot(Order o) {
        return new OrderSnapshot(
                o.getId(),
                o.getCustomerId(),
                o.getTotalPrice(),
                o.getCreatedAt(),
                o.getUpdatedAt(),
                o.getConfirmedAt(),
                o.getStatus().name());
    }
}
