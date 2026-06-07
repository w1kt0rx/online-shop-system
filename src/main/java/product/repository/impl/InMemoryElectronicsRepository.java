package product.repository.impl;

import product.model.electronics.Electronics;
import product.repository.ElectronicsRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryElectronicsRepository implements ElectronicsRepository {
    private final Map<Long, Electronics> database = new ConcurrentHashMap<>();
    private final AtomicLong sequenceId = new AtomicLong(1L);

    @Override
    public Electronics save(Electronics entity) {
        database.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void delete(Long id) {
        database.remove(id);
    }

    @Override
    public Optional<Electronics> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Electronics> getAll() {
        return database.values().stream().toList();
    }

    @Override
    public Long getNextId() {
        return sequenceId.getAndIncrement();
    }
}
