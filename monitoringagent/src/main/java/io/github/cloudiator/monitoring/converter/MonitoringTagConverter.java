package io.github.cloudiator.monitoring.converter;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.rest.model.MonitoringTag;
import org.cloudiator.messages.entities.MonitorEntities;

public class MonitoringTagConverter implements
    TwoWayConverter<MonitoringTag, MonitorEntities.MonitoringTag> {

  @Override
  public MonitoringTag applyBack(MonitorEntities.MonitoringTag kafkaMonitoringTag) {
    MonitoringTag result = new MonitoringTag()
        .key(kafkaMonitoringTag.getKey())
        .value(kafkaMonitoringTag.getValue());
    return result;
  }

  @Override
  public MonitorEntities.MonitoringTag apply(MonitoringTag domainMonitoringTag) {
    MonitorEntities.MonitoringTag.Builder result = MonitorEntities.MonitoringTag.newBuilder()
        .setKey(domainMonitoringTag.getKey())
        .setValue(domainMonitoringTag.getValue());
    return result.build();
  }
}
