package io.github.cloudiator.monitoring.converter;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.rest.model.MonitoringTarget.TypeEnum;
import org.cloudiator.messages.entities.MonitorEntities;
import org.cloudiator.messages.entities.MonitorEntities.TargetType;

public class MonitoringTargetConverter implements
    TwoWayConverter<MonitoringTarget, MonitorEntities.MonitoringTarget> {

  @Override
  public MonitoringTarget applyBack(MonitorEntities.MonitoringTarget kafkaMonitoringTarget) {
    MonitoringTarget result = new MonitoringTarget()
        .type(TypeEnum.fromValue(kafkaMonitoringTarget.getType().name()));
    result.setIdentifier(kafkaMonitoringTarget.getIdentifier());
    return result;
  }

  @Override
  public MonitorEntities.MonitoringTarget apply(MonitoringTarget domainMonitoringTarget) {
    MonitorEntities.MonitoringTarget.Builder result = MonitorEntities.MonitoringTarget.newBuilder()
        .setIdentifier(domainMonitoringTarget.getIdentifier())
        .setType(TargetType.valueOf(domainMonitoringTarget.getType().name()));
    return result.build();
  }
}
