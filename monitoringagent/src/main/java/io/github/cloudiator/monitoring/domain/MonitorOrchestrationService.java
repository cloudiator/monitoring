package io.github.cloudiator.monitoring.domain;

import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.persistance.MonitorModel;
import io.github.cloudiator.rest.model.Monitor;
import java.util.List;
import java.util.Optional;

public interface MonitorOrchestrationService {

  DomainMonitorModel createMonitor(DomainMonitorModel newMonitor, String userid);

  List<DomainMonitorModel> getAllMonitors();

  List<DomainMonitorModel> getAllYourMonitors(String userid);

  List<DomainMonitorModel> getMonitorsOnTarget(String targetId, String userId);

  void updateMonitor(String dbMetric, DomainMonitorModel domainMonitor, String userId);

  void updateMonitorFromRest(MonitorModel dbmonitor, DomainMonitorModel restMonitor, boolean updateSensor,
      boolean updateTag, boolean updateTarget, boolean updateSink);

  DomainMonitorModel deleteMonitor(String monitormetric);

  Optional<DomainMonitorModel> getMonitor(String monitorMetric, String userid);

  List<DomainMonitorModel> getMonitorsWithSameMetric(String metric, String userId);

}
