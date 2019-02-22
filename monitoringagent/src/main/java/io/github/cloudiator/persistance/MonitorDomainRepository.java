package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.rest.model.DataSink;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.MonitoringTag;
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

public class MonitorDomainRepository {

  final static MonitorModelConverter MONITOR_MODEL_CONVERTER = new MonitorModelConverter();


  private final SensorDomainRepository sensorDomainRepository;
  private final TargetDomainRepository targetDomainRepository;

  private final DataSinkModelRepository dataSinkModelRepository;
  private final MonitorModelRepository monitorModelRepository;
  private final IntervalModelRepository intervalModelRepository;


  @Inject
  public MonitorDomainRepository(DataSinkModelRepository dataSinkModelRepository,
      MonitorModelRepository monitorModelRepository,
      TargetDomainRepository targetDomainRepository,
      SensorDomainRepository sensorDomainRepository,
      IntervalModelRepository intervalModelRepository) {
    this.dataSinkModelRepository = dataSinkModelRepository;
    this.monitorModelRepository = monitorModelRepository;
    this.targetDomainRepository = targetDomainRepository;
    this.sensorDomainRepository = sensorDomainRepository;
    this.intervalModelRepository = intervalModelRepository;
  }

  public DomainMonitorModel findMonitorByMetric(String metric) {
    checkNotNull(metric, "Metric is null");
    checkArgument(!metric.isEmpty(), "Metric is empty");
    MonitorModel result = monitorModelRepository.findMonitorByMetric(metric).orElse(null);
    if (result == null) {
      return null;
    }
    return MONITOR_MODEL_CONVERTER.apply(result);
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

  public DomainMonitorModel addMonitor(Monitor monitor) {
    checkNotNull(monitor, "Monitor is null");
    checkState(!exists(monitor.getMetric()), "Monitormetric already exists. ");
    checkNotNull(monitor.getTargets(), "MonitoringTarget is null");
    checkNotNull(monitor.getSensor(), "Sensor is null.");
    checkNotNull(monitor.getSinks(), "Datasinks is null.");
    // checkNotNull(monitor.getTags(), "Tags is null.");

    /**
     * Create new Models
     */

    MonitorModel monitorModel = new MonitorModel().metric(monitor.getMetric());
    //include Targets
    for (MonitoringTarget monitoringTarget : monitor.getTargets()) {
      TargetModel targetModel = new TargetModel()
          .identifier(monitoringTarget.getIdentifier())
          .targetType(TargetType.valueOf(monitoringTarget.getType().name()));
      monitorModel.addTarget(targetModel);
    }

    //include Sensor
    Sensor sensor = monitor.getSensor();
    switch (sensor.getType()) {
      case "PullSensor":
        IntervalModel intervalModel = new IntervalModel();
        intervalModel.setPeriod(((PullSensor) sensor).getInterval().getPeriod());
        intervalModel.setUnit(Unit.valueOf(((PullSensor) sensor).getInterval().getUnit().name()));

        PullSensorModel pullSensorModel = new PullSensorModel()
            .className(((PullSensor) sensor).getClassName())
            .interval(intervalModel).configuration(((PullSensor) sensor).getConfiguration());
        monitorModel.setSensor(pullSensorModel);
        break;
      case "PushSensor":
        PushSensorModel pushSensorModel = new PushSensorModel();
        pushSensorModel.setPort(((PushSensor) sensor).getPort());
        monitorModel.setSensor(pushSensorModel);
        break;
      default:
        throw new IllegalArgumentException(
            "MonitorceationError: No valid Sensor: " + sensor.getType());
    }
    //Sinks
    for (DataSink dataSink : monitor.getSinks()) {
      DataSinkModel createdsink = new DataSinkModel()
          .sinkType(dataSink.getType().name())
          .configuration(dataSink.getConfiguration());

      monitorModel.addDataSink(createdsink);
    }
    //Tags
    Map<String, String> tags = new HashMap<>();
    if (!monitor.getTags().isEmpty()) {
      tags.putAll(monitor.getTags());
    }
    monitorModel.setMonitoringTags(tags);

    /**
     *Save all Models
     * Monitor has CasdaceType.All by all references
     */

    monitorModelRepository.save(monitorModel);

    return MONITOR_MODEL_CONVERTER.apply(monitorModel);
  }

  public void updateMonitor(Monitor monitor) {
    MonitorModel dbMonitor = monitorModelRepository.findMonitorByMetric(monitor.getMetric())
        .orElse(null);
    if (dbMonitor == null) {
      throw new IllegalStateException("Monitor does not exist.");
    }
    // NOT Implemented
  }

  public void deleteMonitor(Monitor monitor) {
    Optional<MonitorModel> dbMonitor = monitorModelRepository
        .findMonitorByMetric(monitor.getMetric());
    if (!dbMonitor.isPresent()) {
      throw new IllegalStateException("Monitor does not exist.");
    } else {
      monitorModelRepository.delete(dbMonitor.get());
    }

  }

  public void deleteMonitor(String metric) {
    Optional<MonitorModel> dbMonitor = monitorModelRepository.findMonitorByMetric(metric);
    if (!dbMonitor.isPresent()) {
      throw new IllegalStateException("Monitor does not exist.");
    } else {
      monitorModelRepository.delete(dbMonitor.get());
    }
  }

  public void deleteAll() {
    monitorModelRepository.deleteAll();
  }


}

