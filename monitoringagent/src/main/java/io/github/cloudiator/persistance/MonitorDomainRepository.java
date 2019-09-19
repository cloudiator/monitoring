package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.rest.model.MonitoringTarget.TypeEnum;
import io.github.cloudiator.rest.model.PullSensor;
import io.github.cloudiator.rest.model.PushSensor;
import io.github.cloudiator.rest.model.Sensor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.stream.Collectors;


public class MonitorDomainRepository {

  final static MonitorModelConverter MONITOR_MODEL_CONVERTER = new MonitorModelConverter();
  final static StateTypeConverter STATE_TYPE_CONVERTER = new StateTypeConverter();

  private final MonitorModelRepository monitorModelRepository;
  private final TargetDomainRepository targetDomainRepository;
  private final SensorDomainRepository sensorDomainRepository;
  private final DataSinkDomainRepository dataSinkDomainRepository;


  @Inject
  public MonitorDomainRepository(MonitorModelRepository monitorModelRepository,
      TargetDomainRepository targetDomainRepository,
      SensorDomainRepository sensorDomainRepository,
      DataSinkDomainRepository dataSinkDomainRepository
  ) {
    this.monitorModelRepository = monitorModelRepository;
    this.targetDomainRepository = targetDomainRepository;
    this.sensorDomainRepository = sensorDomainRepository;
    this.dataSinkDomainRepository = dataSinkDomainRepository;
  }

  public MonitorModel findYourMonitorByMetricAndTarget(String metric, TargetType targetType,
      String targetId, String owner) {
    Optional<MonitorModel> result = monitorModelRepository
        .findYourMonitorByMetricAndTarget(metric, targetType, targetId, owner);
    if (!result.isPresent()) {
      return null;
    } else {
      return result.get();
    }
  }

  public List<MonitorModel> getAllMonitors() {
    List<MonitorModel> result = new ArrayList<>();
    result = monitorModelRepository.findAll();
    return result;
  }

  public List<DomainMonitorModel> getAllYourMonitors(String userid) {
    List<DomainMonitorModel> result = new ArrayList<>();
    result = monitorModelRepository.getAllYourMonitors(userid).stream()
        .map(MONITOR_MODEL_CONVERTER)
        .collect(Collectors.toList());
    return result;
  }

  public List<MonitorModel> findMonitorsOnTarget(String targetId, String owner) {
    List<MonitorModel> result = new ArrayList<>();
    result = monitorModelRepository.findMonitorsOnTarget(targetId, owner);
    return result;
  }

  public List<MonitorModel> findMonitorsOnTarget(TargetType targetType, String targetId) {
    List<MonitorModel> result = new ArrayList<>();
    result = monitorModelRepository.findMonitorsOnTarget(targetType, targetId);
    return result;
  }

  public MonitorModel createDBMonitor(DomainMonitorModel domainMonitorModel, String userid) {

    //Targets
    final List<TargetModel> targetModelList = targetDomainRepository
        .createTargetModelList(domainMonitorModel.getTargets()).stream()
        .collect(Collectors.toList());

    //Sensor
    Sensor sensor = domainMonitorModel.getSensor();
    SensorModel sensorModel;
    switch (sensor.getType()) {
      case "PullSensor":
        sensorModel = sensorDomainRepository.createPullSensor((PullSensor) sensor);
        break;
      case "PushSensor":
        sensorModel = sensorDomainRepository.createPushSensor(((PushSensor) sensor).getPort());
        break;
      default:
        throw new IllegalArgumentException(
            "MonitorceationError: No valid Sensor: " + domainMonitorModel.getSensor().getType());
    }
    final SensorModel monitorSensor = sensorModel;

    //Sinks
    final List<DataSinkModel> dataSinkModelList = dataSinkDomainRepository
        .createDataSinkModelList(domainMonitorModel.getSinks());

    //Tags
    Map<String, String> tags = new HashMap<>();
    if (!domainMonitorModel.getTags().isEmpty()) {
      tags.putAll(domainMonitorModel.getTags());
    }

    //Monitor
    MonitorModel createdModel = new MonitorModel(domainMonitorModel.getMetric(),
        TargetType.valueOf(domainMonitorModel.getOwnTargetType().name()),
        domainMonitorModel.getOwnTargetId(), targetModelList,
        monitorSensor, dataSinkModelList, tags, userid);
    createdModel
        .setOwnTargetState(StateType.valueOf(domainMonitorModel.getOwnTargetState().name()));

    // monitorModel is fully initialized
    monitorModelRepository.save(createdModel);

    return createdModel;
  }

  /* TODO Rest putMonitor not used
  public MonitorModel updateMonitorFromRest(String userId,
      DomainMonitorModel restMonitor,
      boolean updateSensor,
      boolean updateTag, boolean updateTarget, boolean updateSink) {
    checkNotNull(dbMetric, "Monitor is null. ");
    checkNotNull(restMonitor, "Monitor is null. ");

    //Monitor itself
    monitorModelRepository.save(dbmonitor);
    NOT IMPLEMENTED
    return null;
  }
  */

  public MonitorModel updateMonitorUuidTagsState(DomainMonitorModel domainMonitor,
      String userId) {
    Optional<MonitorModel> dbResult = monitorModelRepository
        .findYourMonitorByMetricAndTarget(domainMonitor.getMetric(),
            TargetType.valueOf(domainMonitor.getOwnTargetType().name()),
            domainMonitor.getOwnTargetId(), userId);
    if (!dbResult.isPresent()) {
      throw new IllegalStateException("Monitor does not exist.");
    }
    MonitorModel dbMonitor = dbResult.get();
    //Update VisorUuid
    dbMonitor.setVisorUuid(domainMonitor.getUuid());
    //Update Tags
    dbMonitor.setMonitortags(domainMonitor.getTags());
    //Update ownTargetState
    dbMonitor.setOwnTargetState(StateType.valueOf(domainMonitor.getOwnTargetState().name()));

    //save Monitor
    monitorModelRepository.save(dbMonitor);
    return dbMonitor;
  }

  public MonitorModel updateTargetState(DomainMonitorModel domainMonitor) {
    MonitorModel dbMonitor = monitorModelRepository
        .findMonitorByMetricAndTarget(domainMonitor.getMetric(),
            TargetType.valueOf(domainMonitor.getOwnTargetType().name()),
            domainMonitor.getOwnTargetId()).get();
    dbMonitor.setOwnTargetState(StateType.valueOf(domainMonitor.getOwnTargetState().name()));
    //save Monitor
    monitorModelRepository.save(dbMonitor);
    return dbMonitor;
  }


  public MonitorModel deleteMonitor(DomainMonitorModel domainMonitorModel, String userId) {
    Optional<MonitorModel> monitorModel = monitorModelRepository
        .findYourMonitorByMetricAndTarget(domainMonitorModel.getMetric(),
            TargetType.valueOf(domainMonitorModel.getOwnTargetType().name()),
            domainMonitorModel.getOwnTargetId(), userId);
    monitorModelRepository.delete(monitorModel.get());
    return monitorModel.get();
  }


  public MonitorModel findAndDeleteMonitor(String metric, MonitoringTarget monitoringTarget,
      String userId) {
    Optional<MonitorModel> dbMonitor = monitorModelRepository
        .findYourMonitorByMetricAndTarget(metric,
            TargetType.valueOf(monitoringTarget.getType().name()), monitoringTarget.getIdentifier(),
            userId);
    if (!dbMonitor.isPresent()) {
      throw new IllegalStateException("Monitor does not exist.");
    } else {
      monitorModelRepository.delete(dbMonitor.get());
      return dbMonitor.get();
    }
  }

  public List<MonitorModel> findAllMonitorsWithSameMetric(String metric, String userId) {
    List<MonitorModel> result = new ArrayList<>();
    result = monitorModelRepository.findAllMonitorsWithSameMetric(metric, userId);
    return result;
  }

  public int updateTargetStateInMonitors(TargetType targetType, String targetId,
      StateType stateType) {
    int result = monitorModelRepository
        .updateTargetStateInMonitors(targetType, targetId, stateType);
    return result;
  }

  public int getMonitorCount() {
    int result = monitorModelRepository.getMonitorCount();
    return result;
  }

  public List<MonitorModel> getNumberedMonitors(int begin, int number) {
    List<MonitorModel> result = new ArrayList<>();
    result = monitorModelRepository.getNumberedMonitors(begin, number).stream()
        .collect(Collectors.toList());
    return result;
  }


}

