package io.github.cloudiator.monitoring.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.name.Named;
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
  private final int retryAttmepts;
  private final int minWaitingTime;
  private final int maxWaitingTime;


  @Inject
  public BasicMonitorOrchestrationService(MonitorDomainRepository monitorDomainRepository,
      @Named("retryAttempts") int retryAttempts, @Named("minWaitingTime") int minWaitingTime,
      @Named("maxWaitingTime") int maxWaitingTime) {
    this.monitorDomainRepository = monitorDomainRepository;
    this.retryAttmepts = retryAttempts;
    this.minWaitingTime = minWaitingTime;
    this.maxWaitingTime = maxWaitingTime;
  }

  @Override
  public DomainMonitorModel createMonitor(DomainMonitorModel newMonitor, String userid) {
    MonitorModel result = TransactionRetryer
        .retry(minWaitingTime, maxWaitingTime, retryAttmepts,
            () -> repeatedCreation(newMonitor, userid));
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
        .retry(minWaitingTime, maxWaitingTime, retryAttmepts,
            () -> repeatedGetMonitorsOnTarget(nodeId, userid));
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
        .retry(minWaitingTime, maxWaitingTime, retryAttmepts,
            () -> repeatedRestUpdate(dbmonitor, restMonitor, updateSensor, updateTag, updateTarget,
                updateSink));
  }

  @Transactional
  public MonitorModel repeatedRestUpdate(MonitorModel dbmonitor, DomainMonitorModel restMonitor,
      boolean updateSensor,
      boolean updateTag, boolean updateTarget, boolean updateSink) {
    MonitorModel result = monitorDomainRepository
        .updateMonitorFromRest(dbmonitor, restMonitor, updateSensor, updateTag, updateTarget,
            updateSink);
    return result;
  }

  @Override
  public void updateMonitor(MonitorModel dbmonitor) {
    TransactionRetryer
        .retry(minWaitingTime, maxWaitingTime, retryAttmepts,
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
  public Optional<MonitorModel> getMonitor(String DbMetric, String userid) {
    checkNotNull(DbMetric, "Metric is null");
    MonitorModel result = monitorDomainRepository
        .findMonitorByMetric(DbMetric, userid);
    /*
    final MonitorModel result = TransactionRetryer
        .retry(minWaitingTime, maxWaitingTime, retryAttmepts,
            () -> repeatedGetMonitor(DbMetric, userid));
    */
    if (result == null) {
      return Optional.empty();
    } else {
      return Optional.of(result);
    }
  }

  @Transactional
  public MonitorModel repeatedGetMonitor(String DbMetric, String userId) {
    MonitorModel result = monitorDomainRepository
        .findMonitorByMetric(DbMetric, userId);
    return result;
  }


  @Override
  public List<MonitorModel> getMonitorsWithSameMetric(String metric, String userId) {
    List<MonitorModel> result =
        TransactionRetryer
            .retry(minWaitingTime, maxWaitingTime, retryAttmepts,
                () -> repeatedGetMonitorsWithSameMetric(metric, userId));
    return result;
  }

  @Transactional
  public List<MonitorModel> repeatedGetMonitorsWithSameMetric(String metric, String userId) {
    return monitorDomainRepository.findAllMonitorsWithSameMetric(metric, userId);
  }


}
