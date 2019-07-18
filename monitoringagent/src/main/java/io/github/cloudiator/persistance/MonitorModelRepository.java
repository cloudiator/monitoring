package io.github.cloudiator.persistance;

import java.util.List;
import java.util.Optional;

public interface MonitorModelRepository extends ModelRepository<MonitorModel> {

  Optional<MonitorModel> findMonitorByMetricAndTarget(String metric, TargetType targetType,
      String targetId);

  Optional<MonitorModel> findYourMonitorByMetricAndTarget(String metric, TargetType targetType,
      String targetId, String owner);

  List<MonitorModel> findMonitorsOnTarget(String targetId, String owner);

  List<MonitorModel> getAllYourMonitors(String userid);

  List<MonitorModel> findAllMonitorsWithSameMetric(String metric, String owner);


}
