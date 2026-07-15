package product.repository.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import product.model.smartphone.Smartphone;
import product.repository.SmartphoneRepository;

public class InMemorySmartphoneRepository implements SmartphoneRepository {

    private final Map<Long, Smartphone> database = new ConcurrentHashMap<>();
    private final AtomicLong sequenceId = new AtomicLong(1L);

    @Override
    public Smartphone save(Smartphone entity) {
        if (entity.getId() == null) {
            entity.setId(getNextId());
        }
        database.put(entity.getId(), entity);
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

    private Long getNextId() {
        return sequenceId.getAndIncrement();
    }
}
