package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.Query;

@Deprecated
public class MTagModelRepositoryJpa extends
    BaseModelRepositoryJpa<MTagModel> implements MTagModelRepository {

  @Inject
  protected MTagModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<MTagModel> type) {
    super(entityManager, type);
  }

  @Override
  public Optional<MTagModel> findByKeyValuePair(String tagKey, String value) {
    checkNotNull(tagKey, "TagKey is null.");
    String queryString = String
        .format("from %s where tagKey=:tagKey and value=:value", type.getName());
    Query query = em().createQuery(queryString).setParameter("tagKey", tagKey)
        .setParameter("value", value);
    return Optional.of((MTagModel) query.getSingleResult());
  }
}
