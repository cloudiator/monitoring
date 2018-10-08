package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import java.util.Collection;
import javax.persistence.EntityManager;

public class PushSensorModelRepositoryJpa extends BaseModelRepositoryJpa<PushSensorModel> implements
    PushSensorModelRepository {

  @Inject
  protected PushSensorModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<PushSensorModel> type) {
    super(entityManager, type);
  }


}
