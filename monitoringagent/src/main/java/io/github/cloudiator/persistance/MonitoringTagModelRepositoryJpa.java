package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public class MonitoringTagModelRepositoryJpa extends
    BaseModelRepositoryJpa<MonitoringTagModel> implements MonitoringTagModelRepository {

  @Inject
  protected MonitoringTagModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<MonitoringTagModel> type) {
    super(entityManager, type);
  }

  @Override
  public Optional<MonitoringTagModel> findByKeyValuePair(String tagKey, String value) {
    checkNotNull(tagKey, "TagKey is null.");
    String queryString = String
        .format("from %s where tagKey=:tagKey and value=:value", type.getName());
    Query query = em().createQuery(queryString).setParameter("tagKey", tagKey)
        .setParameter("value", value);
    return Optional.of((MonitoringTagModel) query.getSingleResult());
  }
}
