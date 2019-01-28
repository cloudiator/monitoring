package io.github.cloudiator.persistance;

import java.util.Optional;

public interface MonitorModelRepository extends ModelRepository<MonitorModel> {

  Optional<MonitorModel> findMonitorByMetric(String metric);

}
