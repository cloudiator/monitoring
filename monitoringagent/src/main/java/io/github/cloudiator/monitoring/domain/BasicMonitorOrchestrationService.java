package io.github.cloudiator.monitoring.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.protobuf.MapEntry;
import io.github.cloudiator.monitoring.converter.MonitorToVisorMonitorConverter;

import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.persistance.MonitorDomainRepository;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.MonitoringTarget;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BasicMonitorOrchestrationService implements MonitorOrchestrationService {

  private final MonitorDomainRepository monitorDomainRepository;


  @Inject
  public BasicMonitorOrchestrationService(MonitorDomainRepository monitorDomainRepository) {
    this.monitorDomainRepository = monitorDomainRepository;
  }

  @Override
  public DomainMonitorModel createMonitor(Monitor newMonitor) {
    DomainMonitorModel result = null;
    for (MonitoringTarget target : newMonitor.getTargets()) {
      Monitor test = new Monitor().metric(
          newMonitor.getMetric().concat("+++").concat(target.getType().name()).concat("+++")
              .concat(target.getIdentifier()));
      test.setSensor(newMonitor.getSensor());
      test.setSinks(newMonitor.getSinks());
      test.setTargets(newMonitor.getTargets());
      Map<String, String> tagtargets = newMonitor.getTags();
      tagtargets.put(target.getType().name(), target.getIdentifier());
      test.setTags(tagtargets);
      result = monitorDomainRepository.addMonitor(test);
    }
    return result;
  }

  @Override
  public List<DomainMonitorModel> getAllMonitors() {
    return monitorDomainRepository
        .getAllMonitors();
  }

  @Override
  public void updateMonitor(Monitor monitor) {
    checkNotNull(monitor, "Monitor is null.");
    //Not implemented
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
  public Optional<DomainMonitorModel> getMonitor(String monitorMetric) {
    checkNotNull(monitorMetric, "Metric is null");
    final DomainMonitorModel result = monitorDomainRepository.findMonitorByMetric(monitorMetric);
    if (result == null) {
      return Optional.empty();
    } else {
      return Optional.of(result);
    }
  }


}
