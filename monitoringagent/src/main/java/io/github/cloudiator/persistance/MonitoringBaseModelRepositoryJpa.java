package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import java.util.List;
import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * BaseModelRepositoryJpa for Monitoring same as in common only separated
 */
public class MonitoringBaseModelRepositoryJpa<T extends BaseModel> implements
    BaseModelRepository<T> {

  protected final Class<T> type;
  @SuppressWarnings("unused")
  private final Provider<EntityManager> entityManager;

  @Inject
  protected MonitoringBaseModelRepositoryJpa(Provider<EntityManager> entityManager,
      TypeLiteral<T> type) {
    //noinspection unchecked
    this.type = (Class<T>) type.getRawType();
    this.entityManager = entityManager;
  }

  protected EntityManager em() {
    //todo: replace with correct call to jpaAPI
    //todo: currently blocked by https://github.com/playframework/playframework/issues/4890
    return entityManager.get();
  }

  @Override
  @Nullable
  public T findById(Long id) {
    checkNotNull(id);
    return em().find(type, id);
  }

  private void persist(final T t) {
    em().persist(t);
  }

  @Override
  public void save(final T t) {
    checkNotNull(t);
    if (t.getId() == null) {
      this.persist(t);
    } else {
      this.update(t);
    }
    this.flush();
    this.refresh(t);
  }

  protected T update(final T t) {
    return em().merge(t);
  }

  private void flush() {
    em().flush();
  }

  private T refresh(final T t) {
    em().refresh(t);
    return t;
  }

  private T refreshModel(final T t) {
    return t;
  }

  @Override
  public void delete(final T t) {
    checkNotNull(t);
    em().remove(t);
  }


  @Override
  public List<T> findAll() {
    String queryString = String.format("from %s", type.getName());
    Query query = em().createQuery(queryString);
    //noinspection unchecked
    return query.getResultList();
  }
}
