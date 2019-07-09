package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import io.github.cloudiator.util.JpaResultHelper;
import java.util.Optional;
import javax.persistence.EntityManager;

public class TargetModelRepositoryJpa extends MonitoringBaseModelRepositoryJpa<TargetModel> implements
    TargetModelRepository {

  @Inject
  protected TargetModelRepositoryJpa(Provider<EntityManager> entityManager,
      TypeLiteral<TargetModel> type) {
    super(entityManager, type);
  }


  @Override
  public Optional<TargetModel> getByIdentifierAndType(String identifier, TargetType targetType) {
    String query = String
        .format("select t from %s t where t.identifier=:identifier and t.targetType=:targetType", type.getName());
    final TargetModel targetModel = (TargetModel) JpaResultHelper
        .getSingleResultOrNull(em().createQuery(query).setParameter("identifier", identifier)
            .setParameter("targetType", targetType));
    return Optional.ofNullable(targetModel);
  }
}
