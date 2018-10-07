package org.xlrnet.wk.dashboardprogressserver.common;

import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Optional;

/**
 * Abstract base class for all services which perform transactional operations.
 */
@Transactional
public abstract class AbstractTransactionalService<T extends AbstractEntity, R extends PagingAndSortingRepository<T, String>> {

    /**
     * The concrete {@link CrudRepository} for this service.
     */
    private final R crudRepository;

    /**
     * Constructor for abstract transactional service. Needs always a crud repository for performing operations.
     *
     * @param crudRepository
     *         The crud repository for providing basic crud operations.
     */
    public AbstractTransactionalService(R crudRepository) {
        this.crudRepository = crudRepository;
    }

    /**
     * Deletes the given entity from database.
     *
     * @param entity
     *         The entity to delete.
     */
    @Transactional
    public void delete(T entity) {
        crudRepository.delete(entity);
    }

    /**
     * Returns the entity with a given id.
     *
     * @param id
     *         The id to find.
     * @return the entity with a given id.
     */
    @Transactional(readOnly = true)
    public Optional<T> findById(String id) {
        return crudRepository.findById(id);
    }

    /**
     * Returns all entities in the repository.
     *
     * @return all entities.
     */
    @Transactional(readOnly = true)
    public Iterable<T> findAll() {
        return crudRepository.findAll();
    }

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed
     * the entity instance completely.
     *
     * @param entity
     *         The entity to save.
     * @return the saved entity
     */
    @Transactional
    public <S extends T> S save(S entity) {
        return crudRepository.save(entity);
    }

    /**
     * Saves all given entities.
     *
     * @param entities
     *         The entities to save.
     * @return the saved entities.
     * @throws IllegalArgumentException
     *         in case the given entity is {@literal null}.
     */
    @Transactional
    public <S extends T> Iterable<S> saveAll(Iterable<S> entities) {
        return crudRepository.saveAll(entities);
    }

    /**
     * Finds all entities sorted by the given {@link Sort} parameter.
     *
     * @param sort
     *         The sort order to use.
     * @param <S>
     *         The entity that will be returned.
     * @return All entities sorted by the given parameter.
     */
    @Transactional(readOnly = true)
    public <S extends T> Collection<S> findAll(Sort sort) {
        return (Collection<S>) getRepository().findAll(sort);
    }

    /**
     * Counts all entities.
     *
     * @return number of entities.
     */
    @Transactional(readOnly = true)
    public long countAll() {
        return getRepository().count();
    }

    /**
     * Returns the crud service for this service.
     *
     * @return the crud service for this service.
     */
    protected R getRepository() {
        return crudRepository;
    }

    /**
     * Refreshes the given entity from the database.
     *
     * @param entity
     *         The entity to refresh.
     * @return The refreshed entity.
     */
    public T refresh(T entity) {
        return getRepository().findById(entity.getId()).get();
    }
}
