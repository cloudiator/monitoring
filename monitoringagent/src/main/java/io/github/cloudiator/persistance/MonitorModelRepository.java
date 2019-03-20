package io.github.cloudiator.persistance;

import java.util.List;
import java.util.Optional;

public interface MonitorModelRepository extends BaseModelRepository<MonitorModel> {

  Optional<MonitorModel> findMonitorByMetric(String metric);

  void deleteAll();


}
