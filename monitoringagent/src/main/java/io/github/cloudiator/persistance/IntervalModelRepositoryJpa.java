package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public class IntervalModelRepositoryJpa extends MonitoringBaseModelRepositoryJpa<IntervalModel> implements
    IntervalModelRepository {

  @Inject
  protected IntervalModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<IntervalModel> type) {
    super(entityManager, type);
  }


}
