package order.repository.impl.file;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import order.model.Order;
import order.repository.OrderRepository;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FileOrderRepository implements OrderRepository {
    private final File file;
    private final ObjectMapper mapper;
    private final Map<Long, Order> cache = new HashMap<>();
    private Long nextId = 1L;

    public FileOrderRepository(String filePath) {
        this.file = new File(filePath);
        this.mapper = new ObjectMapper();
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
        persistToFile();
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public List<Order> getAll() {
        return List.copyOf(cache.values());
    }

    @Override
    public Long getNextId() {
        return nextId++;
    }

    private void loadFromFile() {
        if(!file.exists()) return;

        try {
            List<OrderSnapshot> snapshots = mapper.readValue(
                    file, new TypeReference<List<OrderSnapshot>>() {});

            snapshots.forEach(snapshot -> {
                if (snapshot.id() >= nextId) nextId = snapshot.id() + 1;
            });
        } catch (IOException e) {
            System.err.println("Could not load orders from file: " + e.getMessage());
        }
    }

    private void persistToFile() {
        try {
            List<OrderSnapshot> snapshots = cache.values().stream()
                    .map(order -> new OrderSnapshot(
                            order.getId(),
                            order.getCustomerId(),
                            order.getTotalPrice(),
                            order.getCreatedAt(),
                            order.getUpdatedAt(),
                            order.getConfirmedAt(),
                            order.getStatus().name()))
                    .toList();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, snapshots);
        } catch (IOException e) {
            System.err.println("Could not persist orders: " + e.getMessage());
        }
    }
}
