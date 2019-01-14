package io.github.cloudiator.monitoring.converter;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.monitoring.domain.VisorPushMonitorModel;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.PushSensor;

public class MonitorToPushMonitorConverter extends MonitorToVisorMonitorConverter implements
    OneWayConverter<Monitor, VisorPushMonitorModel> {

  @Override
  public VisorPushMonitorModel apply(Monitor monitor) {
    VisorPushMonitorModel result = new VisorPushMonitorModel()
        .metricName(monitor.getMetric())
        .type("PushMonitor")
        .dataSinks(monitor.getSinks());
    PushSensor sensor = (PushSensor) monitor.getSensor();
    result.setPort(sensor.getPort());

    // componentId?
    // monitorContext?

    return result;
  }

}
