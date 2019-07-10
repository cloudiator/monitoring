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
import java.util.stream.Collectors;

public class BasicMonitorOrchestrationService implements MonitorOrchestrationService {

  private final MonitorDomainRepository monitorDomainRepository;
  private final MonitorModelConverter monitorModelConverter = MonitorModelConverter.INSTANCE;
  private final int retryAttempts;
  private final int minWaitingTime;
  private final int maxWaitingTime;


  @Inject
  public BasicMonitorOrchestrationService(MonitorDomainRepository monitorDomainRepository,
      @Named("retryAttempts") int retryAttempts, @Named("minWaitingTime") int minWaitingTime,
      @Named("maxWaitingTime") int maxWaitingTime) {
    this.monitorDomainRepository = monitorDomainRepository;
    this.retryAttempts = retryAttempts;
    this.minWaitingTime = minWaitingTime;
    this.maxWaitingTime = maxWaitingTime;
  }

  @Override
  public DomainMonitorModel createMonitor(DomainMonitorModel newMonitor, String userid) {
    DomainMonitorModel result = TransactionRetryer
        .retry(minWaitingTime, maxWaitingTime, retryAttempts,
            () -> repeatedCreation(newMonitor, userid));
    return result;
  }

  @Transactional
  public DomainMonitorModel repeatedCreation(DomainMonitorModel Monitor, String userid) {
    MonitorModel result = monitorDomainRepository.createDBMonitor(Monitor, userid);
    return monitorModelConverter.apply(result);
  }

  @Override
  public List<DomainMonitorModel> getMonitorsOnTarget(String nodeId, String userid) {
    List<DomainMonitorModel> result = TransactionRetryer
        .retry(minWaitingTime, maxWaitingTime, retryAttempts,
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
        .retry(minWaitingTime, maxWaitingTime, retryAttempts,
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
  public void updateMonitor(String monitorMetric,DomainMonitorModel dbmonitor,String userId) {
    TransactionRetryer
        .retry(minWaitingTime, maxWaitingTime, retryAttempts,
            () -> repeatedUpdate(monitorMetric, dbmonitor, userId));
  }

  @Transactional
  public MonitorModel repeatedUpdate(String monitorMetirc,DomainMonitorModel domainMonitor, String userId) {
    MonitorModel result = monitorDomainRepository
        .updateMonitor(monitorMetirc,domainMonitor, userId);
    return result;
  }

  @Override
  public DomainMonitorModel deleteMonitor(String metric) {
    DomainMonitorModel result = TransactionRetryer
        .retry(minWaitingTime, maxWaitingTime, retryAttempts, () -> repeatedDeleteMonitor(metric));
    return result;
  }


  @Transactional
  public DomainMonitorModel repeatedDeleteMonitor(String metric) {
    MonitorModel result = monitorDomainRepository.deleteMonitor(metric);
    return monitorModelConverter.apply(result);
  }

  @Override
  public Optional<DomainMonitorModel> getMonitor(String DbMetric, String userid) {
    checkNotNull(DbMetric, "Metric is null");

    DomainMonitorModel result = TransactionRetryer
        .retry(minWaitingTime, maxWaitingTime, retryAttempts,
            () -> repeatedGetMonitor(DbMetric, userid));

    if (result == null) {
      return Optional.empty();
    } else {
      return Optional.of(result);
    }
  }

  @Transactional
  public DomainMonitorModel repeatedGetMonitor(String DbMetric, String userId) {
    MonitorModel result = monitorDomainRepository
        .findYourMonitorByMetric(DbMetric, userId);
    return monitorModelConverter.apply(result);
  }


  @Override
  public List<DomainMonitorModel> getMonitorsWithSameMetric(String metric, String userId) {
    List<DomainMonitorModel> result =
        TransactionRetryer
            .retry(minWaitingTime, maxWaitingTime, retryAttempts,
                () -> repeatedGetMonitorsWithSameMetric(metric, userId));
    return result;
  }

  @Transactional
  public List<DomainMonitorModel> repeatedGetMonitorsWithSameMetric(String metric, String userId) {
    List<MonitorModel> result = monitorDomainRepository
        .findAllMonitorsWithSameMetric(metric, userId);
    return result.stream().map(monitorModel -> monitorModelConverter.apply(monitorModel))
        .collect(Collectors.toList());


  }


}
