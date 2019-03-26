package io.github.cloudiator.monitoring.domain;

import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.persistance.MonitorModel;
import io.github.cloudiator.rest.model.Monitor;
import java.util.List;
import java.util.Optional;

public interface MonitorOrchestrationService {

  MonitorModel createMonitor(DomainMonitorModel newMonitor);

  List<DomainMonitorModel> getAllMonitors();

  void updateMonitor(MonitorModel monitor);

  void deleteMonitor(String monitormetric);

  void deleteAll();

  Optional<MonitorModel> getMonitor(String monitorMetric);

  MonitorModel persistMonitor(MonitorModel monitorModel);

}
