package io.github.cloudiator.monitoring.converter;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.rest.model.DataSink;
import io.github.cloudiator.rest.model.Interval;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.PullSensor;
import io.github.cloudiator.rest.model.PushSensor;
import io.github.cloudiator.visor.rest.model.DataSink.TypeEnum;
import io.github.cloudiator.visor.rest.model.DataSinkConfiguration;
import io.github.cloudiator.visor.rest.model.Interval.TimeUnitEnum;
import io.github.cloudiator.visor.rest.model.PushMonitor;
import io.github.cloudiator.visor.rest.model.SensorMonitor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MonitorToVisorMonitorConverter implements
    OneWayConverter<Monitor, io.github.cloudiator.visor.rest.model.Monitor> {

  private final DataSinkConverter dataSinkConverter = new DataSinkConverter();
  private final IntervalConverter intervalConverter = new IntervalConverter();


  @Override
  public io.github.cloudiator.visor.rest.model.Monitor apply(Monitor monitor) {
    io.github.cloudiator.visor.rest.model.Monitor result = null;
    if (monitor.getSensor() instanceof PushSensor) {
      result = new PushMonitor()
          .port(new BigDecimal(((PushSensor) monitor.getSensor()).getPort()))
          .metricName(monitor.getMetric())
          .dataSinks(dataSinkConverter.apply(monitor.getSinks()))
          .type(io.github.cloudiator.visor.rest.model.Monitor.TypeEnum.PUSHMONITOR)
          .componentId("1");
      //missing ComponentId, MonitorContext
    } else if (monitor.getSensor() instanceof PullSensor) {
      result = new SensorMonitor()
          .interval(intervalConverter.apply(((PullSensor) monitor.getSensor()).getInterval()))
          .sensorClassName(((PullSensor) monitor.getSensor()).getClassName())
          .sensorConfiguration(((PullSensor) monitor.getSensor()).getConfiguration())
          .metricName(monitor.getMetric())
          .dataSinks(dataSinkConverter.apply(monitor.getSinks()))
          .type(io.github.cloudiator.visor.rest.model.Monitor.TypeEnum.SENSORMONITOR)
          .componentId("1");
      //missing ComponentId, MonitorContext
    } else {
      throw new IllegalStateException("Unkown Sensortype: " + monitor.getSensor().getType());
    }
    return result;
  }


  private class DataSinkConverter implements
      OneWayConverter<List<DataSink>, List<io.github.cloudiator.visor.rest.model.DataSink>> {

    @Override
    public List<io.github.cloudiator.visor.rest.model.DataSink> apply(List<DataSink> dataSinks) {
      List<io.github.cloudiator.visor.rest.model.DataSink> result = new ArrayList<>();
      for (DataSink monitorsink : dataSinks) {
        result.add(new io.github.cloudiator.visor.rest.model.DataSink()
            .type(TypeEnum.valueOf(monitorsink.getType().toString()))
            .config(new DataSinkConfiguration().values(monitorsink.getConfiguration())));
      }
      return result;
    }
  }

  private class IntervalConverter implements
      OneWayConverter<Interval, io.github.cloudiator.visor.rest.model.Interval> {

    @Override
    public io.github.cloudiator.visor.rest.model.Interval apply(Interval interval) {
      io.github.cloudiator.visor.rest.model.Interval result = new io.github.cloudiator.visor.rest.model.Interval()
          .period(new BigDecimal(interval.getPeriod()))
          .timeUnit(TimeUnitEnum.fromValue(interval.getUnit().toString()));
      return result;
    }
  }
}


