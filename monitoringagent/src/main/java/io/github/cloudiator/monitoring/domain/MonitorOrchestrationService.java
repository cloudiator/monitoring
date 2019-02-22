package io.github.cloudiator.monitoring.domain;

import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.rest.model.Monitor;
import java.util.List;
import java.util.Optional;

public interface MonitorOrchestrationService {

  DomainMonitorModel createMonitor(Monitor newMonitor);

  List<DomainMonitorModel> getAllMonitors();

  void updateMonitor(Monitor monitor);

  void deleteMonitor(Monitor monitor);

  void deleteMonitor(String monitormetric);

  void deleteAll();

  Optional<DomainMonitorModel> getMonitor(String monitorMetric);

}
