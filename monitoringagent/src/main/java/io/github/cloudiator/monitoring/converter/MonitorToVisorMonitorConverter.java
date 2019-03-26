package io.github.cloudiator.monitoring.converter;


import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.rest.model.DataSink;
import io.github.cloudiator.rest.model.Interval;
import io.github.cloudiator.rest.model.Interval.UnitEnum;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.rest.model.PullSensor;
import io.github.cloudiator.rest.model.PushSensor;
import io.github.cloudiator.visor.rest.model.DataSink.TypeEnum;
import io.github.cloudiator.visor.rest.model.DataSinkConfiguration;
import io.github.cloudiator.visor.rest.model.Interval.TimeUnitEnum;
import io.github.cloudiator.visor.rest.model.PushMonitor;
import io.github.cloudiator.visor.rest.model.SensorMonitor;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MonitorToVisorMonitorConverter implements
    TwoWayConverter<DomainMonitorModel, io.github.cloudiator.visor.rest.model.Monitor> {

  public static final MonitorToVisorMonitorConverter INSTANCE = new MonitorToVisorMonitorConverter();

  private final DataSinkConverter dataSinkConverter = new DataSinkConverter();
  private final IntervalConverter intervalConverter = new IntervalConverter();

  private MonitorToVisorMonitorConverter() {
  }


  @Override
  public io.github.cloudiator.visor.rest.model.Monitor apply(DomainMonitorModel monitor) {
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
    result.setUuid(monitor.getUuid());
    return result;
  }

  @Override
  public DomainMonitorModel applyBack(io.github.cloudiator.visor.rest.model.Monitor visorMonitor) {
    DomainMonitorModel result = new DomainMonitorModel()
        .metric(visorMonitor.getMetricName());
    result.setSinks(dataSinkConverter.applyBack(visorMonitor.getDataSinks()));
    result.setTags(visorMonitor.getMonitorContext());
    result.setUuid(visorMonitor.getUuid());
    switch (visorMonitor.getType()) {
      case PUSHMONITOR:
        PushSensor pushSensor = new PushSensor()
            .port(((PushMonitor) visorMonitor).getPort().intValue());
        pushSensor.setType(pushSensor.getClass().getSimpleName().toString());
        result.setSensor(pushSensor);
        break;
      case SENSORMONITOR:
        PullSensor pullSensor = new PullSensor()
            .interval(intervalConverter.applyBack(((SensorMonitor) visorMonitor).getInterval()))
            ._configuration(((SensorMonitor) visorMonitor).getSensorConfiguration());
        result.setSensor(pullSensor);
        break;
      default:
        throw new IllegalStateException("unkown Monitortyp: " + visorMonitor.getType());
    }

    return result;
  }


  private class DataSinkConverter implements
      TwoWayConverter<List<DataSink>, List<io.github.cloudiator.visor.rest.model.DataSink>> {

    @Override
    public List<io.github.cloudiator.visor.rest.model.DataSink> apply(List<DataSink> dataSinks) {
      List<io.github.cloudiator.visor.rest.model.DataSink> result = new ArrayList<>();
      for (DataSink monitorsink : dataSinks) {
        result.add(new io.github.cloudiator.visor.rest.model.DataSink()
            .type(TypeEnum.valueOf(monitorsink.getType().name()))
            .config(new DataSinkConfiguration().values(monitorsink.getConfiguration())));
      }
      return result;
    }

    @Override
    public List<DataSink> applyBack(
        List<io.github.cloudiator.visor.rest.model.DataSink> visorDataSinks) {
      List<DataSink> result = new ArrayList<>();
      for (io.github.cloudiator.visor.rest.model.DataSink visorDataSink : visorDataSinks) {
        result.add(new DataSink()
            .type(DataSink.TypeEnum.valueOf(visorDataSink.getType().name()))
            ._configuration(visorDataSink.getConfig().getValues()));
      }
      return result;
    }
  }

  private class IntervalConverter implements
      TwoWayConverter<Interval, io.github.cloudiator.visor.rest.model.Interval> {

    @Override
    public io.github.cloudiator.visor.rest.model.Interval apply(Interval interval) {
      io.github.cloudiator.visor.rest.model.Interval result = new io.github.cloudiator.visor.rest.model.Interval()
          .period(new BigDecimal(interval.getPeriod()))
          .timeUnit(TimeUnitEnum.fromValue(interval.getUnit().name()));
      return result;
    }

    @Override
    public Interval applyBack(io.github.cloudiator.visor.rest.model.Interval VisorInterval) {
      Interval result = new Interval()
          .period(VisorInterval.getPeriod().longValue())
          .unit(UnitEnum.valueOf(VisorInterval.getTimeUnit().getValue()));
      return result;
    }
  }
}


