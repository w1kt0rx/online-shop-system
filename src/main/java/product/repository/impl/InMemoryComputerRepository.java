package product.repository.impl;

import product.model.computer.Computer;
import product.repository.ComputerRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryComputerRepository implements ComputerRepository {
    private final Map<Long, Computer> database = new ConcurrentHashMap<>();
    private final AtomicLong sequenceId = new AtomicLong(1L);

    @Override
    public Computer save(Computer entity) {
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
    public Optional<Computer> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Computer> getAll() {
        return database.values().stream().toList();
    }

    private Long getNextId() {
        return sequenceId.getAndIncrement();
    }
}
