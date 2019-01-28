package io.github.cloudiator.monitoring.domain;

import io.github.cloudiator.rest.model.Monitor;
import java.util.List;
import java.util.Optional;

public interface MonitorOrchestrationService {

  Monitor createMonitor(Monitor newMonitor);

  List<io.github.cloudiator.visor.rest.model.Monitor> getAllMonitors();

  void updateMonitor(Monitor monitor);

  void deleteMonitor(Monitor monitor);

  void deleteMonitor(String monitormetric);

  Optional<Monitor> getMonitor(String monitorMetric);

}
