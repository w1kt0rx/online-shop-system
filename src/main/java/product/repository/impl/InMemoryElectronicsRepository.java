package product.repository.impl;

import product.model.electronics.Electronics;
import product.repository.ElectronicsRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryElectronicsRepository implements ElectronicsRepository {
    private final Map<Long, Electronics> database = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public Electronics save(Electronics entity) {
        database.putIfAbsent(entity.getId(), entity);
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
        return nextId++;
    }
}
