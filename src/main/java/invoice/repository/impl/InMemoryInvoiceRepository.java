package invoice.repository.impl;

import invoice.model.Invoice;
import invoice.repository.InvoiceRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryInvoiceRepository implements InvoiceRepository {
    private final Map<Long, Invoice> database = new HashMap<>();
    private Long nextId = 1L;

    @Override
    public Invoice save(Invoice entity) {
        database.put(entity.getId(), entity);
        return entity;
    }

    @Override
    public void delete(Long id) {
        database.remove(id);
    }

    @Override
    public Optional<Invoice> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Invoice> getAll() {
        return database.values().stream().toList();
    }

    @Override
    public Long getNextId() {
        return nextId++;
    }
}
