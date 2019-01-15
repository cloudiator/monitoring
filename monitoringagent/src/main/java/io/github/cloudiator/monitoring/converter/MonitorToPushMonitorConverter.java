package io.github.cloudiator.monitoring.converter;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.PushSensor;
import io.github.cloudiator.visor.rest.model.Monitor.TypeEnum;
import io.github.cloudiator.visor.rest.model.PushMonitor;

public class MonitorToPushMonitorConverter implements
    OneWayConverter<Monitor, PushMonitor> {

  @Override
  public PushMonitor apply(Monitor monitor) {
    PushMonitor result = new PushMonitor();
    result.metricName(monitor.getMetric())
        .setType(TypeEnum.PUSHMONITOR);


    // componentId?
    // monitorContext?

    return result;
  }

}
