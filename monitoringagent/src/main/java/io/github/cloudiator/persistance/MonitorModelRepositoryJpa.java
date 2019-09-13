package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import io.github.cloudiator.util.JpaResultHelper;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public class MonitorModelRepositoryJpa extends
    BaseModelRepositoryJpa<MonitorModel> implements
    MonitorModelRepository {

  @Inject
  protected MonitorModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<MonitorModel> type) {
    super(entityManager, type);
  }

  @Override
  public Optional<MonitorModel> findMonitorByMetricAndTarget(String metric, TargetType targetType,
      String targetId) {

    String query = String.format(
        "select m from %s m  where m.metric=:metric and m.ownTargetType=:ownTargetType and m.ownTargetId=:ownTargetId",
        type.getName());
    final MonitorModel monitorModel = (MonitorModel) JpaResultHelper
        .getSingleResultOrNull(em().createQuery(query).setParameter("metric", metric)
            .setParameter("ownTargetType", targetType).setParameter("ownTargetId", targetId));
    return Optional.ofNullable(monitorModel);
  }

  @Override
  public Optional<MonitorModel> findYourMonitorByMetricAndTarget(String metric,
      TargetType targetType, String targetId, String owner) {
    String query = String.format(
        "select m from %s m where m.metric=:metric and m.owner=:owner and m.ownTargetType=:targetType and m.ownTargetId=:targetId",
        type.getName());
    final MonitorModel monitorModel = (MonitorModel) JpaResultHelper
        .getSingleResultOrNull(
            em().createQuery(query).setParameter("metric", metric).setParameter("owner", owner)
                .setParameter("targetId", targetId).setParameter("targetType", targetType));
    return Optional.ofNullable(monitorModel);
  }

  @Override
  public List<MonitorModel> findMonitorsOnTarget(String targetId, String owner) {
    String queryString = String
        .format("select m from %s m where m.owner =:owner and m.metric like :metric",
            type.getName());
    Query query = em().createQuery(queryString).setParameter("owner", owner)
        .setParameter("metric", "%" + targetId);
    //noinspection unchecked
    return query.getResultList();
  }

  @Override
  public List<MonitorModel> findMonitorsOnTarget(TargetType targetType, String targetId,
      String owner) {
    String queryString = String
        .format(
            "select m from %s m where m.owner =:owner and m.ownTargetType like :targetType and m.ownTargetId like :targetId",
            type.getName());
    Query query = em().createQuery(queryString).setParameter("owner", owner)
        .setParameter("targetType", targetType)
        .setParameter("targetId", targetId);
    //noinspection unchecked
    return query.getResultList();
  }

  @Override
  public List<MonitorModel> getAllYourMonitors(String owner) {
    String queryString = String
        .format("select m from %s m where m.owner=:owner", type.getName());
    Query query = em().createQuery(queryString).setParameter("owner", owner);
    //noinspection unchecked
    return query.getResultList();
  }

  @Override
  public List<MonitorModel> findAllMonitorsWithSameMetric(String metric, String owner) {
    String queryString = String
        .format("from %s where owner =:owner and metric like :metric", type.getName());
    Query query = em().createQuery(queryString).setParameter("owner", owner)
        .setParameter("metric", metric + "%");
    //noinspection unchecked
    return query.getResultList();
  }


}
