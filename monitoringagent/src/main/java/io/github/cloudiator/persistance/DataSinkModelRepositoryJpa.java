package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import io.github.cloudiator.util.JpaResultHelper;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.Query;

public class DataSinkModelRepositoryJpa extends BaseModelRepositoryJpa<DataSinkModel> implements
    DataSinkModelRepository {

  @Inject
  protected DataSinkModelRepositoryJpa(
      Provider<EntityManager> entityManager,
      TypeLiteral<DataSinkModel> type) {
    super(entityManager, type);
  }

  @Override
  public List<DataSinkModel> getAllOfType(DataSinkType dataSinkType) {
    checkNotNull(dataSinkType, "DataSinkType is null.");
    String queryString = String.format("from %s where dataSinkType=:dataSinkType", type.getName());
    Query query = em().createQuery(queryString).setParameter("dataSinkType", dataSinkType);
    return (List<DataSinkModel>) query.getResultList();
  }

  @Override
  public List<DataSinkModel> getDatasinksFromMonitor(MonitorModel monitorModel) {
    checkNotNull(monitorModel, "MonitorModel is null");

    return null;
  }


}
