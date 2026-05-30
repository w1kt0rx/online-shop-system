package customer.repository.impl;

import customer.repository.CustomerRepository;
import customer.model.Customer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryCustomerRepository implements CustomerRepository {
    private final Map<Long, Customer> database = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public Customer save(Customer entity) {
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

    @Override
    public Long getNextId() {
        return nextId++;
    }
}
