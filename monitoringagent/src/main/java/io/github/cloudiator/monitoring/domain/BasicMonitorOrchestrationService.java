package io.github.cloudiator.monitoring.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.persistance.MonitorDomainRepository;
import io.github.cloudiator.persistance.MonitorModel;
import io.github.cloudiator.persistance.MonitorModelConverter;
import java.util.ArrayList;
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
  public List<DomainMonitorModel> getMonitorsOnTarget(String nodeId, String userid) {
    List<DomainMonitorModel> result = TransactionRetryer
        .retry(500, 5000, 5, () -> repeatedGetMonitorsOnTarget(nodeId, userid));
    return result;
  }

  @Transactional
  public List<DomainMonitorModel> repeatedGetMonitorsOnTarget(String targetId, String userid) {
    List<DomainMonitorModel> result = monitorDomainRepository
        .findMonitorsOnTarget(targetId, userid);
    return result;
  }


  @Override
  @Transactional
  public List<DomainMonitorModel> getAllMonitors() {
    return monitorDomainRepository
        .getAllMonitors();
  }

  @Override
  @Transactional
  public List<DomainMonitorModel> getAllYourMonitors(String userid) {
    return monitorDomainRepository
        .getAllYourMonitors(userid);
  }

  @Override
  public void updateMonitorFromRest(MonitorModel dbmonitor, DomainMonitorModel restMonitor,
      boolean updateSensor,
      boolean updateTag, boolean updateTarget, boolean updateSink) {
    TransactionRetryer
        .retry(100, 2000, 5,
            () -> repeatedRestUpdate(dbmonitor, restMonitor, updateSensor, updateTag, updateTarget,
                updateSink));
  }

  @Transactional
  public MonitorModel repeatedRestUpdate(MonitorModel dbmonitor, DomainMonitorModel restMonitor,
      boolean updateSensor,
      boolean updateTag, boolean updateTarget, boolean updateSink) {
    MonitorModel result = monitorDomainRepository
        .updateMonitorFromRest(dbmonitor, restMonitor, updateSensor, updateTag, updateTarget, updateSink);
    return result;
  }

  @Override
  public void updateMonitor(MonitorModel dbmonitor) {
    TransactionRetryer
        .retry(100, 2000, 5,
            () -> repeatedUpdate(dbmonitor));
  }

  @Transactional
  public MonitorModel repeatedUpdate(MonitorModel dbmonitor) {
    MonitorModel result = monitorDomainRepository
        .updateMonitor(dbmonitor);
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
  public List<MonitorModel> getMonitorsWithSameMetric(String metric, String userId) {
    List<MonitorModel> result =
        TransactionRetryer
            .retry(100, 2000, 5, () -> repeatedGetMonitorsWithSameMetric(metric, userId));
    return result;
  }

  @Transactional
  public List<MonitorModel> repeatedGetMonitorsWithSameMetric(String metric, String userId) {
    return monitorDomainRepository.findAllMonitorsWithSameMetric(metric, userId);
  }


}
