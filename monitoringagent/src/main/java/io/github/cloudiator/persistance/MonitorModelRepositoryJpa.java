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
  public void deleteAll() {
    String query = String.format("DELETE FROM MonitorModel ");
    Query deletequery = em().createQuery(query);
    deletequery.executeUpdate();
  }

}
