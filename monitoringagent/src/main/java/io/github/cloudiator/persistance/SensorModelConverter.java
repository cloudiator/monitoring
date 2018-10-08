package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.monitoring.domain.Sensor;


public class SensorModelConverter implements OneWayConverter<SensorModel, Sensor> {

  private final PullSensorModelConverter pullSensorModelConverter = new PullSensorModelConverter();
  private final PushSensorModelConverter pushSensorModelConverter = new PushSensorModelConverter();


  @Override
  public Sensor apply(SensorModel sensorModel) {
    if (sensorModel instanceof PullSensorModel) {
      return pullSensorModelConverter.apply((PullSensorModel) sensorModel);
    } else if (sensorModel instanceof PushSensorModel) {
      return pushSensorModelConverter.apply((PushSensorModel) sensorModel);
    } else {
      throw new AssertionError("SensorModelClassError " + sensorModel.getClass());
    }

  }
}
