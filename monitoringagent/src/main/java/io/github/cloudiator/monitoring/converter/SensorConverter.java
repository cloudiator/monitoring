package io.github.cloudiator.monitoring.converter;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.monitoring.domain.PullSensor;
import io.github.cloudiator.monitoring.domain.PushSensor;
import io.github.cloudiator.monitoring.domain.Sensor;
import org.cloudiator.messages.entities.MonitorEntities;

public class SensorConverter implements TwoWayConverter<Sensor, MonitorEntities.Sensor> {

  private final PullSensorConverter pullSensorConverter = new PullSensorConverter();
  private final PushSensorConverter pushSensorConverter = new PushSensorConverter();

  @Override
  public Sensor applyBack(MonitorEntities.Sensor kafkaSensor) {
    Sensor result;
    switch (kafkaSensor.getSensorCase()) {
      case PULLSENSOR:
        result = pullSensorConverter.applyBack(kafkaSensor.getPullsensor());
        break;
      case PUSHSENSOR:
        result = pushSensorConverter.applyBack(kafkaSensor.getPushsensor());
        break;
      case SENSOR_NOT_SET:
      default:
        throw new AssertionError("SensorCase is invalid: " + kafkaSensor.getSensorCase());
    }
    return result;
  }

  @Override
  public MonitorEntities.Sensor apply(Sensor domainSensor) {
    MonitorEntities.Sensor.Builder result = MonitorEntities.Sensor.newBuilder();

    if (domainSensor instanceof PullSensor) {
      result.setPullsensor(pullSensorConverter.apply((PullSensor) domainSensor));
    } else if (domainSensor instanceof PushSensor) {
      result.setPushsensor(pushSensorConverter.apply((PushSensor) domainSensor));
    }else{
      throw new AssertionError("SensorType is invalid: "+domainSensor.getType());
    }
    return result.build();
  }
}
