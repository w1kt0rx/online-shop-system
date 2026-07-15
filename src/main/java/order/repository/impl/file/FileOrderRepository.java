package order.repository.impl.file;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import order.model.Order;
import order.model.OrderStatus;
import order.repository.OrderRepository;

/**
 * File-backed OrderRepository that persists orders as JSON snapshots.
 */
public class FileOrderRepository implements OrderRepository {

    private final Map<Long, Order> cache = new ConcurrentHashMap<>();
    private final List<OrderSnapshot> persistedSnapshots = new ArrayList<>();
    private final AtomicLong idSequence = new AtomicLong(1L);

    private final OrderSnapshotFileStore fileStore;

    public FileOrderRepository(String filePath) {
        ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.fileStore = new OrderSnapshotFileStore(new File(filePath), mapper);
        initializeFromStorage();
    }

    @Override
    public Order save(Order entity) {
        if (entity.getId() == null) {
            entity.setId(getNextId());
        }
        cache.put(entity.getId(), entity);
        persistCurrentState();
        return entity;
    }

    @Override
    public void delete(Long id) {
        cache.remove(id);
        persistedSnapshots.removeIf(snapshot -> snapshot.id().equals(id));
        persistCurrentState();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public List<Order> getAll() {
        return List.copyOf(cache.values());
    }

    private Long getNextId() {
        return idSequence.getAndIncrement();
    }

    public List<OrderSnapshot> getPersistedSnapshots() {
        return List.copyOf(persistedSnapshots);
    }

    public int getPersistedCount() {
        return persistedSnapshots.size();
    }

    private void initializeFromStorage() {
        List<OrderSnapshot> loadedSnapshots = fileStore.load();
        persistedSnapshots.addAll(loadedSnapshots);

        loadedSnapshots.forEach(snapshot -> {
            updateSequence(snapshot.id());
            cache.put(snapshot.id(), restoreOrder(snapshot));
        });
    }

    private void persistCurrentState() {
        Map<Long, OrderSnapshot> snapshotsById = new LinkedHashMap<>();

        persistedSnapshots.forEach(snapshot -> snapshotsById.put(snapshot.id(), snapshot));
        cache.values().forEach(order -> snapshotsById.put(order.getId(), toSnapshot(order)));

        List<OrderSnapshot> mergedSnapshots = new ArrayList<>(snapshotsById.values());

        persistedSnapshots.clear();
        persistedSnapshots.addAll(mergedSnapshots);

        fileStore.save(mergedSnapshots);
    }

    private void updateSequence(Long orderId) {
        if (orderId >= idSequence.get()) {
            idSequence.incrementAndGet();
        }
    }

    private Order restoreOrder(OrderSnapshot snapshot) {
        return Order.restore(
            snapshot.id(),
            snapshot.customerId(),
            snapshot.totalPrice(),
            snapshot.createdAt(),
            snapshot.updatedAt(),
            snapshot.confirmedAt(),
            OrderStatus.valueOf(snapshot.status())
        );
    }

    private OrderSnapshot toSnapshot(Order order) {
        return new OrderSnapshot(
            order.getId(),
            order.getCustomerId(),
            order.getTotalPrice(),
            order.getCreatedAt(),
            order.getUpdatedAt(),
            order.getConfirmedAt(),
            order.getStatus().name()
        );
    }
}
