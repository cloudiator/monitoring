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



  void updateMonitor(MonitorModel monitor);

  MonitorModel deleteMonitor(String monitormetric);

  Optional<MonitorModel> getMonitor(String monitorMetric, String userid);

  List<MonitorModel> getMonitorsWithSameMetric(String metric, String userId);

  MonitorModel persistMonitor(MonitorModel monitorModel);

}
