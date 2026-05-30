package order.repository.impl;

import order.model.Order;
import order.repository.OrderRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryOrderRepository implements OrderRepository {
    private final Map<Long, Order> database = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public Order save(Order entity) {
        database.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void delete(Long id) {
        database.remove(id);
    }

    @Override
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Order> getAll() {
        return database.values().stream().toList();
    }

    @Override
    public Long getNextId() {
        return nextId++;
    }
}
