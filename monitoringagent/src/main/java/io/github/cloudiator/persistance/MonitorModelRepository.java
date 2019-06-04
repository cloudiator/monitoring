package io.github.cloudiator.persistance;

import java.util.List;
import java.util.Optional;

public interface MonitorModelRepository extends BaseModelRepository<MonitorModel> {

  Optional<MonitorModel> findMonitorByMetric(String metric);

  Optional<MonitorModel> findMonitorByMetric(String metric, String owner);

  List<MonitorModel> findMonitorsOnTarget(String targetId,String owner);

  List<MonitorModel> getAllYourMonitors(String userid);


}
