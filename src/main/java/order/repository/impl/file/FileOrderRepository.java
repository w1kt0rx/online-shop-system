package order.repository.impl.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import order.model.Order;
import order.repository.OrderRepository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


public class FileOrderRepository implements OrderRepository {

    private final File file;
    private final ObjectMapper mapper;
    private final Map<Long, Order> cache = new ConcurrentHashMap<>();
    private final List<OrderSnapshot> persistedSnapshots = new ArrayList<>();
    private final AtomicLong idSequence = new AtomicLong(1L);

    public FileOrderRepository(String filePath) {
        this.file = new File(filePath);
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        loadFromFile();
    }

    @Override
    public Order save(Order entity) {
        cache.put(entity.getId(), entity);
        persistToFile();
        return entity;
    }

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
     * Zwraca Orders z bieżącej sesji.
     * Użyj getPersistedSnapshots() żeby zobaczyć historię z poprzednich sesji.
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
     * Zwraca snapshoty załadowane z pliku przy starcie — historia z poprzednich sesji.
     * Przydatne do raportów i wyświetlania historii zamówień.
     */
    public List<OrderSnapshot> getPersistedSnapshots() {
        return List.copyOf(persistedSnapshots);
    }

    /** Liczba snapshotów załadowanych z pliku (poprzednie sesje). */
    public int getPersistedCount() {
        return persistedSnapshots.size();
    }

    // ── private ───────────────────────────────────────────────────────

    private void loadFromFile() {
        if (!file.exists()) return;
        try {
            List<OrderSnapshot> loaded = mapper.readValue(
                    file, new TypeReference<List<OrderSnapshot>>() {});
            persistedSnapshots.addAll(loaded);
            loaded.forEach(s -> {
                if (s.id() >= idSequence.get()) idSequence.set(s.id() + 1);
            });
            System.out.printf("[FileOrderRepository] Loaded %d snapshots from %s%n",
                    loaded.size(), file.getPath());
        } catch (IOException e) {
            System.err.println("[FileOrderRepository] Could not load: " + e.getMessage());
        }
    }

    private void persistToFile() {
        try {
            // Połącz snapshoty z poprzednich sesji + bieżącą sesję
            List<OrderSnapshot> allSnapshots = new ArrayList<>(persistedSnapshots);

            cache.values().forEach(o -> {
                boolean alreadyPersisted = allSnapshots.stream()
                        .anyMatch(s -> s.id().equals(o.getId()));
                if (!alreadyPersisted) {
                    allSnapshots.add(new OrderSnapshot(
                            o.getId(),
                            o.getCustomerId(),
                            o.getTotalPrice(),
                            o.getCreatedAt(),
                            o.getUpdatedAt(),
                            o.getConfirmedAt(),
                            o.getStatus().name()));
                }
            });

            mapper.writerWithDefaultPrettyPrinter().writeValue(file, allSnapshots);
        } catch (IOException e) {
            System.err.println("[FileOrderRepository] Could not persist: " + e.getMessage());
        }
    }
}
