package io.github.cloudiator.monitoring.converter;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.monitoring.domain.VisorSensorMonitorModel;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.PullSensor;

public class MonitorToSensorMonitorConverter implements OneWayConverter<Monitor, VisorSensorMonitorModel> {

  @Override
  public VisorSensorMonitorModel apply(Monitor monitor) {
    VisorSensorMonitorModel result = new VisorSensorMonitorModel()
        .metricName(monitor.getMetric())
        .type("SensorMonitor")
        .dataSinks(monitor.getSinks());
    PullSensor sensor = (PullSensor) monitor.getSensor();
    result.setSensorClassName(sensor.getClassName());
    result.setSensorConfiguration(sensor.getConfiguration());
    result.setInterval(sensor.getInterval());

    // componentId?
    // monitorContext?

    return result;
  }

}
