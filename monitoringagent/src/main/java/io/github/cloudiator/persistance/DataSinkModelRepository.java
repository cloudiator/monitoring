package io.github.cloudiator.persistance;


import java.util.List;
import java.util.Optional;

public interface DataSinkModelRepository extends BaseModelRepository<DataSinkModel> {

  public List<DataSinkModel> getAllOfType(DataSinkType dataSinkType);

  public List<DataSinkModel> getDatasinksFromMonitor(MonitorModel monitorModel);

}
