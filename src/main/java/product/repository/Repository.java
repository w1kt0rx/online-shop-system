package product.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD repository interface used by all product and domain repositories.
 * <p>
 * Provides a minimal contract for storage operations (save, delete, find, list)
 * and auto-incremented ID generation. Concrete implementations may back this
 * interface with in-memory maps, file storage, or any other persistence mechanism.
 * </p>
 *
 * @param <T> the entity type managed by this repository
 */
public interface Repository<T> {

    /**
     * Persists or updates the given entity.
     *
     * @param entity the entity to save; must not be null
     * @return the saved entity (may be the same instance or a copy)
     */
    T save(T entity);

    /**
     * Removes the entity with the given id. No-op if no entity exists with that id.
     *
     * @param id the entity identifier; must not be {null
     */
    void delete(Long id);

    /**
     * Looks up an entity by its id.
     *
     * @param id the entity identifier; must not be null
     * @return an Optional containing the entity, or empty if not found
     */
    Optional<T> findById(Long id);

    /**
     * Returns all entities currently managed by this repository.
     *
     * @return an unmodifiable list; never null, may be empty
     */
    List<T> getAll();

}
