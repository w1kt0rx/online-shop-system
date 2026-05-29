package product.repository.impl;

import product.model.smartphone.Smartphone;
import product.repository.SmartphoneRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemorySmartphoneRepository implements SmartphoneRepository {
    private final Map<Long, Smartphone> database = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public Smartphone save(Smartphone entity) {
        database.putIfAbsent(entity.getId(), entity);
        return entity;
    }

    @Override
    public void delete(Long id) {
        database.remove(id);
    }

    @Override
    public Optional<Smartphone> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Smartphone> getAll() {
        return database.values().stream().toList();
    }

    @Override
    public Long getNextId() {
        return nextId++;
    }
}
