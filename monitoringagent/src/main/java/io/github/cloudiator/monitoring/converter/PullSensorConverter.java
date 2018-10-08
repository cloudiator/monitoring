package io.github.cloudiator.monitoring.converter;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.monitoring.domain.Interval;
import io.github.cloudiator.monitoring.domain.Interval.UnitEnum;
import io.github.cloudiator.monitoring.domain.Property;
import io.github.cloudiator.monitoring.domain.PullSensor;
import io.github.cloudiator.monitoring.domain.SensorConfiguration;
import org.cloudiator.messages.entities.MonitorEntities;
import org.cloudiator.messages.entities.MonitorEntities.Unit;

public class PullSensorConverter implements
    TwoWayConverter<PullSensor, MonitorEntities.PullSensor> {

  @Override
  public PullSensor applyBack(MonitorEntities.PullSensor kafkaPullSensor) {
    SensorConfiguration sensorConfiguration = new SensorConfiguration();
    for (MonitorEntities.ConfigurationProperties confprop : kafkaPullSensor.getConfiguration()
        .getPropertiesList()) {
      sensorConfiguration
          .addPropertiesItem(new Property().key(confprop.getKey()).value(confprop.getValue()));
    }
    PullSensor result = new PullSensor()
        .className(kafkaPullSensor.getClassName())
        .interval(new Interval(UnitEnum.fromValue(kafkaPullSensor.getInterval().getUnit().name()),
            kafkaPullSensor.getInterval().getPeriod()))
        .configuration(sensorConfiguration);
    result.setType(result.getClass().getSimpleName());
    return result;
  }

  @Override
  public MonitorEntities.PullSensor apply(PullSensor domainPullSensor) {
    MonitorEntities.SensorConfiguration.Builder sensorConfig = MonitorEntities.SensorConfiguration
        .newBuilder();
    for (Property property : domainPullSensor.getConfiguration().getProperties()) {
      sensorConfig.addProperties(MonitorEntities.ConfigurationProperties.newBuilder()
          .setKey(property.getKey()).setValue(property.getValue()).build());
    }
    MonitorEntities.PullSensor.Builder result = MonitorEntities.PullSensor.newBuilder()
        .setClassName(domainPullSensor.getClassName())
        .setInterval(MonitorEntities.Interval.newBuilder()
            .setUnit(Unit.valueOf(domainPullSensor.getInterval().getUnit().name()))
            .setPeriod(domainPullSensor.getInterval().getPeriod()).build())
        .setConfiguration(sensorConfig.build());
    return result.build();
  }
}
