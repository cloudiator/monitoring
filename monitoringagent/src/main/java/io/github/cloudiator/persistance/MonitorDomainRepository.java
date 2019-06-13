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
  private final DataSinkModelRepository dataSinkModelRepository;

  @Inject
  public MonitorDomainRepository(MonitorModelRepository monitorModelRepository,
      TargetDomainRepository targetDomainRepository,
      SensorDomainRepository sensorDomainRepository,
      DataSinkModelRepository dataSinkModelRepository
  ) {
    this.monitorModelRepository = monitorModelRepository;
    this.targetDomainRepository = targetDomainRepository;
    this.sensorDomainRepository = sensorDomainRepository;
    this.dataSinkModelRepository = dataSinkModelRepository;
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


  public MonitorModel persistMonitor(MonitorModel monitorModel) {
    monitorModelRepository.save(monitorModel);
    return monitorModel;
  }

  public MonitorModel createDBMonitor(DomainMonitorModel domainMonitorModel, String userid) {
    MonitorModel monitorModel = new MonitorModel()
        .metric(domainMonitorModel.getMetric());
    //Targets
    for (MonitoringTarget target : domainMonitorModel.getTargets()) {
      TargetModel targetModel = targetDomainRepository
          .createTarget(TargetType.valueOf(target.getType().name()), target.getIdentifier());
      monitorModel.addTarget(targetModel);
    }
    //Sensor
    Sensor sensor = domainMonitorModel.getSensor();
    switch (sensor.getType()) {
      case "PullSensor":
        monitorModel.setSensor(sensorDomainRepository.createPullSensor((PullSensor) sensor));
        break;
      case "PushSensor":
        monitorModel
            .setSensor(sensorDomainRepository.createPushSensor(((PushSensor) sensor).getPort()));
        break;
      default:
        throw new IllegalArgumentException(
            "MonitorceationError: No valid Sensor: " + domainMonitorModel.getSensor().getType());
    }
    //Sinks
    for (DataSink dataSink : domainMonitorModel.getSinks()) {
      DataSinkModel createdsink = new DataSinkModel()
          .sinkType(dataSink.getType().name())
          .configuration(dataSink.getConfiguration());

      dataSinkModelRepository.save(createdsink);
      monitorModel.addDataSink(createdsink);
    }
    //Tags
    Map<String, String> tags = new HashMap<>();
    if (!domainMonitorModel.getTags().isEmpty()) {
      tags.putAll(domainMonitorModel.getTags());
    }
    monitorModel.setMonitoringTags(tags);

    monitorModel.setOwner(userid);
    // monitorModel is fully initialized
    monitorModelRepository.save(monitorModel);

    return monitorModel;
  }

  public MonitorModel updateMonitor(MonitorModel monitor) {
    checkNotNull(monitor, "Monitor is null. ");
    //Sensor
    sensorDomainRepository.saveSensor(monitor.getSensor());
    //DataSink
    for (DataSinkModel sink : monitor.getDatasinks()) {
      dataSinkModelRepository.save(sink);
    }
    //Targets
    for (TargetModel tModel : monitor.getTargets()) {
      targetDomainRepository.saveTarget(tModel);
    }
    //Monitor itself
    monitorModelRepository.save(monitor);
    return monitor;
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

