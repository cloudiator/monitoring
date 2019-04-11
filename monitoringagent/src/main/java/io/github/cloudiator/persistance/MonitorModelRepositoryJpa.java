package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import io.github.cloudiator.util.JpaResultHelper;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public class MonitorModelRepositoryJpa extends MonitoringBaseModelRepositoryJpa<MonitorModel> implements
    MonitorModelRepository {

  @Inject
  protected MonitorModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<MonitorModel> type) {
    super(entityManager, type);
  }

  @Override
  public Optional<MonitorModel> findMonitorByMetric(String metric) {

    String query = String.format("from %s where metric=:metric", type.getName());
    final MonitorModel monitorModel = (MonitorModel) JpaResultHelper
        .getSingleResultOrNull(em().createQuery(query).setParameter("metric", metric));
    return Optional.ofNullable(monitorModel);
  }

  @Override
  public List<MonitorModel> getAllYourMonitors(String owner) {
    String queryString = String.format("from %s where owner=:owner", type.getName());
    Query query = em().createQuery(queryString);
    //noinspection unchecked
    return query.getResultList();
  }


}
