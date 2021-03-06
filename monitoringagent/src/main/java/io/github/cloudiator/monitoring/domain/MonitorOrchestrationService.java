package io.github.cloudiator.monitoring.domain;

import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.monitoring.models.TargetState;
import io.github.cloudiator.persistance.MonitorModel;
import io.github.cloudiator.persistance.StateType;
import io.github.cloudiator.persistance.TargetType;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.rest.model.MonitoringTarget.TypeEnum;
import java.util.List;
import java.util.Optional;

public interface MonitorOrchestrationService {

  DomainMonitorModel createMonitor(DomainMonitorModel newMonitor, String userid);

  List<DomainMonitorModel> getAllMonitors();

  List<DomainMonitorModel> getAllYourMonitors(String userid);

  List<DomainMonitorModel> getMonitorsOnTarget(String targetId, String userId);

  List<DomainMonitorModel> getMonitorsOnTarget(TargetType targetType, String targetId);

  void updateMonitor(DomainMonitorModel domainMonitor, String userId);

  void updateTargetState(DomainMonitorModel domainMonitorModel);

  int updateTargetStateInMonitors(TargetType targetType, String targetId,
      TargetState targetState);

  /*
  void updateMonitorFromRest(String dbMetric, String userId, DomainMonitorModel restMonitor,
      boolean updateSensor,
      boolean updateTag, boolean updateTarget, boolean updateSink);
  */

  DomainMonitorModel deleteMonitor(DomainMonitorModel domainMonitorModel, String userId);

  DomainMonitorModel findAndDeleteMonitor(String metric, MonitoringTarget monitoringTarget,
      String userId);

  Optional<DomainMonitorModel> getMonitor(String metric, MonitoringTarget target, String userId);

  boolean existingMonitor(DomainMonitorModel monitorModel, String userId);

  List<String> getMonitorsWithSameMetric(String metric, String userId);

  int getMonitorCount();

  List<DomainMonitorModel> getNumberedMonitors(int begin, int number);

}
