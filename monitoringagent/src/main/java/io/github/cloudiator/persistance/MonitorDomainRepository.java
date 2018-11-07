package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import io.github.cloudiator.monitoring.domain.DataSink;
import io.github.cloudiator.monitoring.domain.Monitor;
import io.github.cloudiator.monitoring.domain.MonitoringTag;
import io.github.cloudiator.monitoring.domain.MonitoringTarget;
import io.github.cloudiator.monitoring.domain.Property;
import io.github.cloudiator.monitoring.domain.PullSensor;
import io.github.cloudiator.monitoring.domain.PushSensor;
import io.github.cloudiator.monitoring.domain.Sensor;
import io.github.cloudiator.persistance.TargetModel.TargetEnum;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MonitorDomainRepository {

  final static MonitorModelConverter MONITOR_MODEL_CONVERTER = new MonitorModelConverter();

  private final TargetDomainRepository targetDomainRepository;
  private final SensorDomainRepository sensorDomainRepository;

  private final MonitoringTagModelRepository monitoringTagModelRepository;
  private final DataSinkModelRepository dataSinkModelRepository;
  private final MonitorModelRepository monitorModelRepository;


  @Inject
  public MonitorDomainRepository(DataSinkModelRepository dataSinkModelRepository,
      MonitoringTagModelRepository monitoringTagModelRepository,
      MonitorModelRepository monitorModelRepository,
      TargetDomainRepository targetDomainRepository,
      SensorDomainRepository sensorDomainRepository) {
    this.dataSinkModelRepository = dataSinkModelRepository;
    this.monitoringTagModelRepository = monitoringTagModelRepository;
    this.monitorModelRepository = monitorModelRepository;
    this.targetDomainRepository = targetDomainRepository;
    this.sensorDomainRepository = sensorDomainRepository;
  }

  public Monitor findMonitorByMetric(String metric) {
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

  public List<Monitor> getAllMonitors() {
    List<Monitor> allMonitors = new ArrayList<>();
    for (MonitorModel monitormodel : monitorModelRepository.findAll()) {
      allMonitors.add(MONITOR_MODEL_CONVERTER.apply(monitormodel));
    }
    return allMonitors;
  }

  public Monitor addMonitor(Monitor monitor) {
    checkNotNull(monitor, "Monitor is null");
    checkState(!exists(monitor.getMetric()), "Monitormetric already exists. ");
    checkNotNull(monitor.getTargets(), "MonitoringTarget is null");
    checkNotNull(monitor.getSensor(), "Sensor is null.");
    checkNotNull(monitor.getSinks(), "Datasinks is null.");
    checkNotNull(monitor.getTags(), "Tags is null.");

    MonitorModel monitorModel = new MonitorModel();
    //Target
    for (MonitoringTarget monitoringTarget : monitor.getTargets()) {
      TargetModel targetModel = targetDomainRepository
          .getByIdentifierAndType(monitoringTarget.getIdentifier(),
              TargetEnum.valueOf(monitoringTarget.getType().name()));
      if (targetModel == null) {
        targetModel = new TargetModel(
            TargetEnum.valueOf(monitoringTarget.getType().name()), monitoringTarget.getIdentifier(),
            new HashSet<MonitorModel>());
      }
      targetModel.addMonitor(monitorModel);
      targetDomainRepository.saveTarget(targetModel);
      monitorModel.addTarget(targetModel);
    }
    //Sensor
    Sensor sensor = monitor.getSensor();
    if (sensor instanceof PullSensor) {
      PullSensorModel pullSensorModel = new PullSensorModel();
      pullSensorModel.setClassName(((PullSensor) sensor).getClassName());

      IntervalModel intervalModel = new IntervalModel();
      intervalModel.setPeriod(((PullSensor) sensor).getInterval().getPeriod());
      intervalModel.setUnit(Unit.valueOf(((PullSensor) sensor).getInterval().getUnit().name()));
      pullSensorModel.setInterval(intervalModel);
      pullSensorModel.setConfiguration(((PullSensor) sensor).getConfiguration());

      pullSensorModel.setMonitor(monitorModel);

      sensorDomainRepository.saveSensor(pullSensorModel);
      monitorModel.setSensor(pullSensorModel);
    } else if (sensor instanceof PushSensor) {
      PushSensorModel pushSensorModel = new PushSensorModel();
      pushSensorModel.setPort(((PushSensor) sensor).getPort());
      pushSensorModel.setMonitor(monitorModel);

      sensorDomainRepository.saveSensor(pushSensorModel);
      monitorModel.setSensor(pushSensorModel);
    }
    //Sinks
    for (DataSink dataSink : monitor.getSinks()) {
      DataSinkModel dataSinkModel = new DataSinkModel(monitorModel,
          DataSinkType.valueOf(dataSink.getType().name()),
          dataSink.getConfiguration());
      monitorModel.addDataSink(dataSinkModel);
      dataSinkModelRepository.save(dataSinkModel);
    }
    //Tags
    for (MonitoringTag monitoringTag : monitor.getTags()) {
      MonitoringTagModel tagModel = monitoringTagModelRepository
          .findByKeyValuePair(monitoringTag.getKey(), monitoringTag.getValue()).orElse(null);
      if (tagModel == null) {
        tagModel = new MonitoringTagModel(monitoringTag.getKey(), monitoringTag.getValue(),
            new HashSet<MonitorModel>());
      }
      tagModel.addMonitor(monitorModel);
    }
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
}

