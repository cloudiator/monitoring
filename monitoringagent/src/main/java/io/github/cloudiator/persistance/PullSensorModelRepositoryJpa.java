package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import io.github.cloudiator.util.JpaResultHelper;
import java.util.Optional;
import javax.persistence.EntityManager;

public class PullSensorModelRepositoryJpa extends MonitoringBaseModelRepositoryJpa<PullSensorModel> implements
    PullSensorModelRepository {

  @Inject
  protected PullSensorModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<PullSensorModel> type) {
    super(entityManager, type);
  }

  @Override
  public Optional<PullSensorModel> findPullSensorByClassName(String className) {
    String query = String.format("select p from %s p where p.className=:className", type.getName());
    final PullSensorModel pullSensorModel = (PullSensorModel) JpaResultHelper
        .getSingleResultOrNull(em().createQuery(query).setParameter("className", className));
    return Optional.ofNullable(pullSensorModel);
  }
}
