package io.github.cloudiator.monitoring.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.persistance.MonitorDomainRepository;
import io.github.cloudiator.persistance.MonitorModel;
import io.github.cloudiator.persistance.MonitorModelConverter;
import java.util.List;
import java.util.Optional;

public class BasicMonitorOrchestrationService implements MonitorOrchestrationService {

  private final MonitorDomainRepository monitorDomainRepository;
  private final MonitorModelConverter monitorModelConverter = MonitorModelConverter.INSTANCE;


  @Inject
  public BasicMonitorOrchestrationService(MonitorDomainRepository monitorDomainRepository) {
    this.monitorDomainRepository = monitorDomainRepository;
  }

  @Override
  public DomainMonitorModel createMonitor(DomainMonitorModel newMonitor, String userid) {
    MonitorModel result = TransactionRetryer
        .retry(500, 5000, 5, () -> repeatedCreation(newMonitor, userid));
    return monitorModelConverter.apply(result);
  }

  @Transactional
  public MonitorModel repeatedCreation(DomainMonitorModel Monitor, String userid) {
    MonitorModel result = monitorDomainRepository.createDBMonitor(Monitor, userid);
    return result;
  }


  @Override

  public List<DomainMonitorModel> getAllMonitors() {
    return monitorDomainRepository
        .getAllMonitors();
  }

  @Override

  public List<DomainMonitorModel> getAllYourMonitors(String userid) {
    return monitorDomainRepository
        .getAllYourMonitors(userid);
  }

  @Override
  public void updateMonitor(MonitorModel monitor) {
    TransactionRetryer
        .retry(100, 2000, 5, () -> repeatedUpdate(monitor));
  }

  @Transactional
  public MonitorModel repeatedUpdate(MonitorModel Monitor) {
    MonitorModel result = monitorDomainRepository.updateMonitor(Monitor);
    return result;
  }


  @Override
  @Transactional
  public MonitorModel deleteMonitor(String metric) {
    return monitorDomainRepository.deleteMonitor(metric);
  }

  @Override
  public Optional<MonitorModel> getMonitor(String monitorMetric, String userid) {
    checkNotNull(monitorMetric, "Metric is null");
    final MonitorModel result = monitorDomainRepository.findMonitorByMetric(monitorMetric, userid);
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
