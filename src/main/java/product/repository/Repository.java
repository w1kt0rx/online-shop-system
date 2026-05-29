package product.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T> {
    T save(T entity);

    void delete(Long id);

    Optional<T> findById(Long id);

    List<T> getAll();

    Long getNextId();
}
