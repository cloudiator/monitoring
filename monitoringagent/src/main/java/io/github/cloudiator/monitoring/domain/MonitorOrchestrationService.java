package io.github.cloudiator.monitoring.domain;

import java.util.Optional;

public interface MonitorOrchestrationService {

  Monitor createMonitor(Monitor newMonitor);

  Iterable<Monitor> getAllMonitors();

  void updateMonitor(Monitor monitor);

  void deleteMonitor(Monitor monitor);

  Optional<Monitor> getMonitor(String monitorMetric);

}
