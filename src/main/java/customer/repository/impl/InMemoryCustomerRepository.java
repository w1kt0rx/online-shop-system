package customer.repository.impl;

import customer.repository.CustomerRepository;
import customer.model.Customer;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryCustomerRepository implements CustomerRepository {
    private final Map<Long, Customer> database = new ConcurrentHashMap<>();
    private final AtomicLong sequenceId = new AtomicLong(1L);

    @Override
    public Customer save(Customer entity) {
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
    public Optional<Customer> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Customer> getAll() {
        return database.values().stream().toList();
    }

    private Long getNextId() {
        return sequenceId.getAndIncrement();
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        String normalized = email.trim().toLowerCase();
        return database.values().stream()
                .filter(c -> c.getEmail().equals(normalized))
                .findFirst();
    }
}