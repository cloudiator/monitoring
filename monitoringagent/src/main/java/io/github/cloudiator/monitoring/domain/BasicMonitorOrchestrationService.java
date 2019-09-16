package io.github.cloudiator.monitoring.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.persistance.MonitorDomainRepository;
import io.github.cloudiator.persistance.MonitorModel;
import io.github.cloudiator.persistance.MonitorModelConverter;
import io.github.cloudiator.persistance.StateType;
import io.github.cloudiator.persistance.TargetType;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.rest.model.MonitoringTarget.TypeEnum;
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
  DomainMonitorModel repeatedCreation(DomainMonitorModel Monitor, String userid) {
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
  List<DomainMonitorModel> repeatedGetMonitorsOnTarget(String targetId, String userid) {
    List<MonitorModel> result = monitorDomainRepository
        .findMonitorsOnTarget(targetId, userid);
    return result.stream().map(monitorModel -> monitorModelConverter.apply(monitorModel)).collect(
        Collectors.toList());
  }

  @Override
  public List<DomainMonitorModel> getMonitorsOnTarget(TargetType targetType, String targetId) {
    List<DomainMonitorModel> result = TransactionRetryer
        .retry(minWaitingTime, maxWaitingTime, retryAttempts,
            () -> repeatedGetMonitorsOnTarget(targetType, targetId));
    return result;
  }

  @Transactional
  List<DomainMonitorModel> repeatedGetMonitorsOnTarget(TargetType targetType, String targetId) {
    List<MonitorModel> result = monitorDomainRepository
        .findMonitorsOnTarget(targetType, targetId);
    return result.stream().map(monitorModel -> monitorModelConverter.apply(monitorModel)).collect(
        Collectors.toList());
  }


  @Override
  @Transactional
  public List<DomainMonitorModel> getAllMonitors() {
    return monitorDomainRepository
        .getAllMonitors().stream().map(monitorModel -> monitorModelConverter.apply(monitorModel))
        .collect(
            Collectors.toList());
  }

  @Override
  @Transactional
  public List<DomainMonitorModel> getAllYourMonitors(String userid) {
    return monitorDomainRepository
        .getAllYourMonitors(userid);
  }

  // NO WORKING INTERFACE IN REST
  @Override
  public void updateMonitorFromRest(String dbMetric, String userId, DomainMonitorModel restMonitor,
      boolean updateSensor,
      boolean updateTag, boolean updateTarget, boolean updateSink) {
  }

   /*
    TransactionRetryer
        .retry(minWaitingTime, maxWaitingTime, retryAttempts,
            () -> repeatedRestUpdate(userId, restMonitor, updateSensor, updateTag, updateTarget,
                updateSink));
  }


  @Transactional
  public DomainMonitorModel repeatedRestUpdate(String userId, DomainMonitorModel restMonitor,
      boolean updateSensor,
      boolean updateTag, boolean updateTarget, boolean updateSink) {
    MonitorModel result = monitorDomainRepository
        .updateMonitorFromRest(userId, restMonitor, updateSensor, updateTag, updateTarget,
            updateSink);
    return monitorModelConverter.apply(result);
  }
  */

  @Override
  public void updateMonitor(DomainMonitorModel dbMonitor, String userId) {
    TransactionRetryer
        .retry(minWaitingTime, maxWaitingTime, retryAttempts,
            () -> repeatedUpdate(dbMonitor, userId));
  }

  @Transactional
  public DomainMonitorModel repeatedUpdate(DomainMonitorModel dbMonitor, String userId) {
    MonitorModel result = monitorDomainRepository
        .updateMonitorUuidAndTags(dbMonitor, userId);
    return monitorModelConverter.apply(result);
  }

  @Override
  public void updateTargetState(DomainMonitorModel dbMonitor) {
    TransactionRetryer
        .retry(minWaitingTime, maxWaitingTime, retryAttempts,
            () -> repeatedUpdateTargetState(dbMonitor));
  }

  @Transactional
  public DomainMonitorModel repeatedUpdateTargetState(DomainMonitorModel dbMonitor) {
    MonitorModel result = monitorDomainRepository
        .updateTargetState(dbMonitor);
    return monitorModelConverter.apply(result);
  }

  @Override
  public DomainMonitorModel deleteMonitor(DomainMonitorModel domainMonitorModel, String userId) {
    DomainMonitorModel result = TransactionRetryer
        .retry(minWaitingTime, maxWaitingTime, retryAttempts,
            () -> repeatedDeleteMonitor(domainMonitorModel, userId));
    return result;
  }

  @Transactional
  DomainMonitorModel repeatedDeleteMonitor(DomainMonitorModel domainMonitorModel, String userId) {
    MonitorModel result = monitorDomainRepository.deleteMonitor(domainMonitorModel, userId);
    return monitorModelConverter.apply(result);
  }

  @Override
  public DomainMonitorModel findAndDeleteMonitor(String metric, MonitoringTarget monitoringTarget,
      String userId) {
    DomainMonitorModel result = TransactionRetryer
        .retry(minWaitingTime, maxWaitingTime, retryAttempts,
            () -> repeatedFindAndDelete(metric, monitoringTarget, userId));
    return result;
  }

  @Transactional
  DomainMonitorModel repeatedFindAndDelete(String metric, MonitoringTarget monitoringTarget,
      String userId) {
    MonitorModel result = monitorDomainRepository
        .findAndDeleteMonitor(metric, monitoringTarget, userId);
    return monitorModelConverter.apply(result);
  }

  @Override
  public Optional<DomainMonitorModel> getMonitor(String metric, MonitoringTarget monitoringTarget,
      String userid) {
    checkNotNull(metric, "Metric is null");
    checkNotNull(monitoringTarget, "MonitoringTarget is null");
    DomainMonitorModel result = TransactionRetryer
        .retry(minWaitingTime, maxWaitingTime, retryAttempts,
            () -> repeatedGetMonitor(metric, TargetType.valueOf(monitoringTarget.getType().name()),
                monitoringTarget.getIdentifier(), userid));
    if (result == null) {
      return Optional.empty();
    } else {
      return Optional.ofNullable(result);
    }
  }

  @Transactional
  public DomainMonitorModel repeatedGetMonitor(String metric, TargetType targetType,
      String targetId, String userId) {
    DomainMonitorModel model = null;
    MonitorModel result = monitorDomainRepository
        .findYourMonitorByMetricAndTarget(metric, targetType, targetId, userId);
    if (result != null) {
      model = monitorModelConverter.apply(result);
    }
    return model;
  }

  @Override
  public boolean existingMonitor(DomainMonitorModel domainMonitorModel, String userId) {
    DomainMonitorModel result = TransactionRetryer
        .retry(minWaitingTime, maxWaitingTime, retryAttempts,
            () -> repeatedGetMonitor(domainMonitorModel.getMetric(),
                TargetType.valueOf(domainMonitorModel.getOwnTargetType().name()),
                domainMonitorModel.getOwnTargetId(), userId));
    if (result == null) {
      return false;
    } else {
      return true;
    }
  }


  @Override
  public List<String> getMonitorsWithSameMetric(String metric, String userId) {
    List<String> result =
        TransactionRetryer
            .retry(minWaitingTime, maxWaitingTime, retryAttempts,
                () -> repeatedGetMonitorsWithSameMetric(metric, userId));
    return result;
  }

  @Transactional
  public List<String> repeatedGetMonitorsWithSameMetric(String metric, String userId) {
    List<MonitorModel> result = monitorDomainRepository
        .findAllMonitorsWithSameMetric(metric, userId);
    List<String> metricList = result.stream().map(monitorModel -> monitorModel.getMetric()).collect(
        Collectors.toList());
    return metricList;
  }

  @Override
  public int updateTargetStateInMonitors(TargetType targetType, String targetId,
      StateType stateType) {
    int result = TransactionRetryer.retry(minWaitingTime, maxWaitingTime, retryAttempts,
        () -> repeatedUpdateTargetState(targetType, targetId, stateType));
    return result;
  }

  @Transactional
  int repeatedUpdateTargetState(TargetType targetType, String targetId, StateType stateType) {
    int result = monitorDomainRepository
        .updateTargetStateInMonitors(targetType, targetId, stateType);
    return result;
  }


}
