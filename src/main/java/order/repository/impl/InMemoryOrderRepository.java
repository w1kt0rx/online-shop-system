package order.repository.impl;

import order.model.Order;
import order.repository.OrderRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryOrderRepository implements OrderRepository {
    private final Map<Long, Order> database = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1L);

    @Override
    public Order save(Order entity) {
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
    public Optional<Order> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Order> getAll() {
        return database.values().stream().toList();
    }

    public Long getNextId() {
        return idSequence.getAndIncrement();
    }
}