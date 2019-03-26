package io.github.cloudiator.monitoring.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.protobuf.MapEntry;
import io.github.cloudiator.monitoring.converter.MonitorToVisorMonitorConverter;

import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.persistance.MonitorDomainRepository;
import io.github.cloudiator.persistance.MonitorModel;
import io.github.cloudiator.persistance.MonitorModelConverter;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.MonitoringTarget;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BasicMonitorOrchestrationService implements MonitorOrchestrationService {

  private final MonitorDomainRepository monitorDomainRepository;
  private final MonitorModelConverter monitorModelConverter = MonitorModelConverter.INSTANCE;


  @Inject
  public BasicMonitorOrchestrationService(MonitorDomainRepository monitorDomainRepository) {
    this.monitorDomainRepository = monitorDomainRepository;
  }

  @Override
  @Transactional
  public MonitorModel createMonitor(DomainMonitorModel newMonitor) {
    return monitorDomainRepository.createDBMonitor(newMonitor);
  }

  @Override
  public List<DomainMonitorModel> getAllMonitors() {
    return monitorDomainRepository
        .getAllMonitors();
  }

  @Override
  @Transactional
  public void updateMonitor(MonitorModel monitor) {
    checkNotNull(monitor, "Monitor is null.");
    monitorDomainRepository.updateMonitor(monitor);
  }

  @Override
  public void deleteMonitor(String metric) {
    monitorDomainRepository.deleteMonitor(metric);
  }

  @Override
  public void deleteAll() {
    monitorDomainRepository.deleteAll();
  }

  @Override
  public Optional<MonitorModel> getMonitor(String monitorMetric) {
    checkNotNull(monitorMetric, "Metric is null");
    final MonitorModel result = monitorDomainRepository.findMonitorByMetric(monitorMetric);
    if (result == null) {
      return Optional.empty();
    } else {
      return Optional.of(result);
    }
  }

  @Override
  public MonitorModel persistMonitor(MonitorModel monitorModel) {
    monitorDomainRepository.persistMonitor(monitorModel);
    return monitorModel;
  }


}
