package io.github.cloudiator.monitoring.converter;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.monitoring.domain.Interval;
import io.github.cloudiator.monitoring.domain.Interval.UnitEnum;
import io.github.cloudiator.monitoring.domain.PullSensor;
import org.cloudiator.messages.entities.MonitorEntities;
import org.cloudiator.messages.entities.MonitorEntities.Unit;

public class PullSensorConverter implements
    TwoWayConverter<PullSensor, MonitorEntities.PullSensor> {

  @Override
  public PullSensor applyBack(MonitorEntities.PullSensor kafkaPullSensor) {
    PullSensor result = new PullSensor()
        .className(kafkaPullSensor.getClassName())
        .interval(new Interval(UnitEnum.fromValue(kafkaPullSensor.getInterval().getUnit().name()),
            kafkaPullSensor.getInterval().getPeriod()))
        .configuration(kafkaPullSensor.getConfigurationMap());
    result.setType(result.getClass().getSimpleName());
    return result;
  }

  @Override
  public MonitorEntities.PullSensor apply(PullSensor domainPullSensor) {
    MonitorEntities.PullSensor.Builder result = MonitorEntities.PullSensor.newBuilder()
        .setClassName(domainPullSensor.getClassName())
        .setInterval(MonitorEntities.Interval.newBuilder()
            .setUnit(Unit.valueOf(domainPullSensor.getInterval().getUnit().name()))
            .setPeriod(domainPullSensor.getInterval().getPeriod()).build())
        .putAllConfiguration(domainPullSensor.getConfiguration());
    return result.build();
  }
}
