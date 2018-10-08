package io.github.cloudiator.monitoring.converter;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.monitoring.domain.DataSink;
import io.github.cloudiator.monitoring.domain.Monitor;
import io.github.cloudiator.monitoring.domain.MonitoringTag;
import io.github.cloudiator.monitoring.domain.MonitoringTarget;
import org.cloudiator.messages.entities.MonitorEntities;

public class MonitorConverter implements TwoWayConverter<Monitor, MonitorEntities.Monitor> {

  public static final MonitorConverter MONITOR_CONVERTER = new MonitorConverter();

  private MonitorConverter() {
  }

  private final DataSinkConverter dataSinkConverter = new DataSinkConverter();
  private final MonitoringTagConverter monitoringTagConverter = new MonitoringTagConverter();
  private final MonitoringTargetConverter monitoringTargetConverter = new MonitoringTargetConverter();
  private final SensorConverter sensorConverter = new SensorConverter();

  @Override
  public Monitor applyBack(MonitorEntities.Monitor kafkaMonitor) {
    Monitor result = new Monitor(kafkaMonitor.getMetric());
    //Targets
    for (MonitorEntities.MonitoringTarget kafkaTarget : kafkaMonitor.getTargetList()) {
      result.addTargetsItem(monitoringTargetConverter.applyBack(kafkaTarget));
    }
    //Sensor
    result.setSensor(sensorConverter.applyBack(kafkaMonitor.getSensor()));
    //DataSinks
    for (MonitorEntities.Sink sink : kafkaMonitor.getDatasinkList()) {
      result.addSinksItem(dataSinkConverter.applyBack(sink));
    }
    //Tags
    for (MonitorEntities.MonitoringTag kafkaTag : kafkaMonitor.getTagsList()) {
      result.addTagsItem(monitoringTagConverter.applyBack(kafkaTag));
    }
    return result;
  }

  @Override
  public MonitorEntities.Monitor apply(Monitor domainMonitor) {
    MonitorEntities.Monitor.Builder MonitorBuilder = MonitorEntities.Monitor.newBuilder()
        .setMetric(domainMonitor.getMetric());
    //Targets
    for (MonitoringTarget domainTarget : domainMonitor.getTargets()) {
      MonitorBuilder.addTarget(monitoringTargetConverter.apply(domainTarget));
    }
    //Sensor
    MonitorBuilder.setSensor(sensorConverter.apply(domainMonitor.getSensor()));
    //DataSinks
    for (DataSink datasink : domainMonitor.getSinks()) {
      MonitorBuilder.addDatasink(dataSinkConverter.apply(datasink));
    }
    //Tags
    for (MonitoringTag domainTag : domainMonitor.getTags()) {
      MonitorBuilder.addTags(monitoringTagConverter.apply(domainTag));
    }
    return MonitorBuilder.build();
  }
}
