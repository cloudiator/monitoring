package io.github.cloudiator.monitoring.converter;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.rest.model.DataSink;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.MonitoringTag;
import io.github.cloudiator.rest.model.MonitoringTarget;
import java.util.HashMap;
import java.util.Map;
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
    Monitor result = new Monitor().metric(kafkaMonitor.getMetric());
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
    Map<String, String> tags = new HashMap<>();
    if (!kafkaMonitor.getTagsMap().isEmpty()) {
      tags.putAll(kafkaMonitor.getTagsMap());
    }
    result.setTags(tags);
    return result;
  }

  @Override
  public MonitorEntities.Monitor apply(Monitor domainMonitor) {
    MonitorEntities.Monitor.Builder MonitorBuilder = MonitorEntities.Monitor.newBuilder()
        .setMetric(domainMonitor.getMetric());
    //Targets
    if (domainMonitor.getTargets() != null) {
      for (MonitoringTarget domainTarget : domainMonitor.getTargets()) {
        MonitorBuilder.addTarget(monitoringTargetConverter.apply(domainTarget));
      }
    } else {
      MonitorBuilder.clearTarget();
    }
    //Sensor
    MonitorBuilder.setSensor(sensorConverter.apply(domainMonitor.getSensor()));
    //DataSinks
    for (DataSink datasink : domainMonitor.getSinks()) {
      MonitorBuilder.addDatasink(dataSinkConverter.apply(datasink));
    }
    //Tags
    Map<String, String> tags = new HashMap<>();
    if (!domainMonitor.getTags().isEmpty()) {
      tags.putAll(domainMonitor.getTags());
    }
    MonitorBuilder.putAllTags(tags);
    return MonitorBuilder.build();
  }
}
