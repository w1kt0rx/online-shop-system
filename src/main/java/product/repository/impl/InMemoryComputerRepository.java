package product.repository.impl;

import product.model.computer.Computer;
import product.repository.ComputerRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryComputerRepository implements ComputerRepository {
    private final Map<Long, Computer> database = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public Computer save(Computer entity) {
        database.putIfAbsent(entity.getId(), entity);
        return entity;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public Optional<Computer> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Computer> getAll() {
        return List.of();
    }

    @Override
    public Long getNextId() {
        return nextId++;
    }
}
