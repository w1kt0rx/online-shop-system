package discount.repositoy.impl;

import discount.model.Discount;
import discount.repositoy.DiscountRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryDiscountRepository implements DiscountRepository {
    private final Map<Long, Discount> database = new ConcurrentHashMap<>();
    private final AtomicLong sequenceId = new AtomicLong(1L);

    @Override
    public Optional<Discount> findByCode(String code) {
        return database.values().stream()
                .filter(discount -> discount.getCode().equalsIgnoreCase(code))
                .findFirst();
    }

    @Override
    public Discount save(Discount entity) {
        database.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void delete(Long id) {
        database.remove(id);
    }

    @Override
    public Optional<Discount> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Discount> getAll() {
        return List.copyOf(database.values());
    }

    @Override
    public Long getNextId() {
        return sequenceId.getAndIncrement();
    }
}
