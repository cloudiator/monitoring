package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.rest.model.DataSink;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.rest.model.PullSensor;
import io.github.cloudiator.rest.model.PushSensor;
import io.github.cloudiator.rest.model.Sensor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MonitorDomainRepository {

  final static MonitorModelConverter MONITOR_MODEL_CONVERTER = new MonitorModelConverter();

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

  public MonitorModel findMonitorByMetric(String metric, String owner) {
    checkNotNull(metric, "Metric is null");
    checkArgument(!metric.isEmpty(), "Metric is empty");
    MonitorModel result = monitorModelRepository.findMonitorByMetric(metric, owner).orElse(null);
    if (result == null) {
      return null;
    }
    return result;
  }

  public boolean exists(String metric) {
    checkNotNull(metric, "Metric is null");
    checkArgument(!metric.isEmpty(), "Metric is empty");
    return monitorModelRepository.findMonitorByMetric(metric).isPresent();
  }

  public List<DomainMonitorModel> getAllMonitors() {
    List<DomainMonitorModel> result = new ArrayList<>();
    result = monitorModelRepository.findAll().stream()
        .map(MONITOR_MODEL_CONVERTER)
        .collect(Collectors.toList());
    return result;
  }

  public List<DomainMonitorModel> getAllYourMonitors(String userid) {
    List<DomainMonitorModel> result = new ArrayList<>();
    result = monitorModelRepository.getAllYourMonitors(userid).stream()
        .map(MONITOR_MODEL_CONVERTER)
        .collect(Collectors.toList());
    return result;
  }

  public List<DomainMonitorModel> findMonitorsOnTarget(String targetId, String owner) {
    List<DomainMonitorModel> result = new ArrayList<>();
    result = monitorModelRepository.findMonitorsOnTarget(targetId, owner).stream()
        .map(MONITOR_MODEL_CONVERTER).collect(
            Collectors.toList());
    return result;
  }

  public MonitorModel createDBMonitor(DomainMonitorModel domainMonitorModel, String userid) {
    /* OLD
    MonitorModel monitorModel = new MonitorModel()
        .metric(domainMonitorModel.getMetric());
    */
    //Targets
    List<TargetModel> targetModelList = targetDomainRepository
        .createTargetModelList(domainMonitorModel.getTargets());

    /* OLD
    for (MonitoringTarget target : domainMonitorModel.getTargets()) {
      TargetModel targetModel = targetDomainRepository
          .createTarget(TargetType.valueOf(target.getType().name()), target.getIdentifier(), monitorModel);
      monitorModel.addTarget(targetModel);
    }
    */

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
    //Sinks

    List<DataSinkModel> dataSinkModelList = dataSinkDomainRepository
        .createDataSinkModelList(domainMonitorModel.getSinks());
    /* OLD
    for (DataSink dataSink : domainMonitorModel.getSinks()) {
      DataSinkModel createdsink = new DataSinkModel()
          .sinkType(dataSink.getType().name())
          .configuration(dataSink.getConfiguration());

      dataSinkModelRepository.save(createdsink);
      monitorModel.addDataSink(createdsink);
    }
    */
    //Tags
    Map<String, String> tags = new HashMap<>();
    if (!domainMonitorModel.getTags().isEmpty()) {
      tags.putAll(domainMonitorModel.getTags());
    }

    MonitorModel createdModel = new MonitorModel(domainMonitorModel.getMetric(), targetModelList,
        sensorModel, dataSinkModelList, tags, userid);

    // monitorModel is fully initialized
    monitorModelRepository.save(createdModel);

    return createdModel;
  }

  public MonitorModel updateMonitorFromRest(MonitorModel dbmonitor, DomainMonitorModel restMonitor,
      boolean updateSensor,
      boolean updateTag, boolean updateTarget, boolean updateSink) {
    checkNotNull(dbmonitor, "Monitor is null. ");
    checkNotNull(restMonitor, "Monitor is null. ");
    //Sensor
    if (updateSensor) {
      Sensor sensor = restMonitor.getSensor();
      switch (sensor.getType()) {
        case "PullSensor":
          dbmonitor.setSensor(sensorDomainRepository.createPullSensor((PullSensor) sensor));
          break;
        case "PushSensor":
          dbmonitor
              .setSensor(sensorDomainRepository.createPushSensor(((PushSensor) sensor).getPort()));
          break;
        default:
          throw new IllegalArgumentException(
              "MonitorceationError: No valid Sensor: " + restMonitor.getSensor().getType());
      }
    }
    //DataSink
    if (updateSink) {
      List<DataSinkModel> dataSinkModelList = dataSinkDomainRepository
          .createDataSinkModelList(restMonitor.getSinks());
      dbmonitor.setDatasinks(dataSinkModelList);
      /* OLD
      dbmonitor.setDatasinks(new ArrayList<>());
      for (DataSink dataSink : restMonitor.getSinks()) {
        DataSinkModel createdsink = new DataSinkModel()
            .sinkType(dataSink.getType().name())
            .configuration(dataSink.getConfiguration());

        dataSinkModelRepository.save(createdsink);
        dbmonitor.addDataSink(createdsink);
      }
      */
    }
    //Targets
    if (updateTarget) {
      List<TargetModel> targetModelList = targetDomainRepository
          .createTargetModelList(restMonitor.getTargets());
      dbmonitor.setTargets(targetModelList);
      /* OLD
      dbmonitor.setTargets(new ArrayList<>());
      for (MonitoringTarget target : restMonitor.getTargets()) {
        TargetModel targetModel = targetDomainRepository
            .createTarget(TargetType.valueOf(target.getType().name()), target.getIdentifier(),
                dbmonitor);
        dbmonitor.addTarget(targetModel);
      }
      */
    }
    //Tags
    if (updateTag) {
      Map<String, String> tags = new HashMap<>();
      if (!restMonitor.getTags().isEmpty()) {
        tags.putAll(restMonitor.getTags());
      }
      dbmonitor.setMonitoringTags(tags);
    }
    //Monitor itself
    monitorModelRepository.save(dbmonitor);
    return dbmonitor;
  }

  public MonitorModel updateMonitor(MonitorModel dbMonitor) {
    monitorModelRepository.save(dbMonitor);
    return dbMonitor;
  }


  public MonitorModel deleteMonitor(String metric) {
    Optional<MonitorModel> dbMonitor = monitorModelRepository.findMonitorByMetric(metric);
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

}

