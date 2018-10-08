package io.github.cloudiator.persistance;


import java.util.List;

public interface DataSinkModelRepository extends ModelRepository<DataSinkModel> {

  public List<DataSinkModel> getAllOfType(DataSinkType dataSinkType);


}
