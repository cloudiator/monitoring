package io.github.cloudiator.monitoring.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import io.github.cloudiator.monitoring.converter.MonitorToVisorMonitorConverter;

import io.github.cloudiator.persistance.MonitorDomainRepository;
import io.github.cloudiator.rest.model.Monitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BasicMonitorOrchestrationService implements MonitorOrchestrationService {

  private final MonitorDomainRepository monitorDomainRepository;
  private final MonitorToVisorMonitorConverter visorMonitorConverter = new MonitorToVisorMonitorConverter();


  @Inject
  public BasicMonitorOrchestrationService(MonitorDomainRepository monitorDomainRepository) {
    this.monitorDomainRepository = monitorDomainRepository;
  }

  @Override
  public Monitor createMonitor(Monitor newMonitor) {
    return monitorDomainRepository.addMonitor(newMonitor);
  }

  @Override
  public List<Monitor> getAllMonitors() {
    /*
    List<Monitor> result = new ArrayList<>();
    for (io.github.cloudiator.rest.model.Monitor monitor : monitorDomainRepository
        .getAllMonitors()) {
      result.add(visorMonitorConverter.apply(monitor));
    }
    */
    return monitorDomainRepository
        .getAllMonitors();
  }

  @Override
  public void updateMonitor(Monitor monitor) {
    checkNotNull(monitor, "Monitor is null.");
    //Not implemented
  }

  @Override
  public void deleteMonitor(Monitor monitor) {
    monitorDomainRepository.deleteMonitor(monitor);
  }

  @Override
  public void deleteMonitor(String metric) {
    monitorDomainRepository.deleteMonitor(metric);
  }

  @Override
  public Optional<Monitor> getMonitor(String monitorMetric) {
    checkNotNull(monitorMetric, "Metric is null");
    final Monitor result = monitorDomainRepository.findMonitorByMetric(monitorMetric);
    if (result == null) {
      return Optional.empty();
    }
    return Optional.of(result);
  }


}
