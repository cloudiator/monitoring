package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import io.github.cloudiator.persistance.TargetModel.TargetEnum;
import io.github.cloudiator.util.JpaResultHelper;
import java.util.Optional;
import javax.persistence.EntityManager;

public class TargetModelRepositoryJpa extends BaseModelRepositoryJpa<TargetModel> implements
    TargetModelRepository {

  @Inject
  protected TargetModelRepositoryJpa(Provider<EntityManager> entityManager,
      TypeLiteral<TargetModel> type) {
    super(entityManager, type);
  }


  @Override
  public Optional<TargetModel> getByIdentifierAndType(String identifier, TargetEnum targetEnum) {
    String query = String
        .format("from %s where identifier=:identifier and targetEnum=:targetEnum", type.getName());
    final TargetModel targetModel = (TargetModel) JpaResultHelper
        .getSingleResultOrNull(em().createQuery(query).setParameter("identifier", identifier)
            .setParameter("targetEnum", targetEnum));
    return Optional.ofNullable(targetModel);
  }
}
