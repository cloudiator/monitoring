package io.github.cloudiator.monitoring.converter;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.rest.model.Interval;
import io.github.cloudiator.rest.model.Interval.UnitEnum;
import io.github.cloudiator.rest.model.PullSensor;
import org.cloudiator.messages.entities.CommonEntities;
import org.cloudiator.messages.entities.MonitorEntities;
import org.cloudiator.messages.entities.CommonEntities.Unit;

public class PullSensorConverter implements
    TwoWayConverter<PullSensor, MonitorEntities.PullSensor> {

  @Override
  public PullSensor applyBack(MonitorEntities.PullSensor kafkaPullSensor) {
    PullSensor result = new PullSensor()
        .className(kafkaPullSensor.getClassName())
        .interval(
            new Interval().unit(UnitEnum.valueOf(kafkaPullSensor.getInterval().getUnit().name()))
                .period(kafkaPullSensor.getInterval().getPeriod()))
        ._configuration(kafkaPullSensor.getConfigurationMap());
    result.setType(result.getClass().getSimpleName());
    return result;
  }

  @Override
  public MonitorEntities.PullSensor apply(PullSensor domainPullSensor) {
    MonitorEntities.PullSensor.Builder result = MonitorEntities.PullSensor.newBuilder()
        .setClassName(domainPullSensor.getClassName())
        .setInterval(CommonEntities.Interval.newBuilder()
            .setUnit(Unit.valueOf(domainPullSensor.getInterval().getUnit().name()))
            .setPeriod(domainPullSensor.getInterval().getPeriod()).build());
    if (!domainPullSensor.getConfiguration().isEmpty()) {
      result.putAllConfiguration(domainPullSensor.getConfiguration());
    } else {
      result.clearConfiguration();
    }
    return result.build();
  }
}
