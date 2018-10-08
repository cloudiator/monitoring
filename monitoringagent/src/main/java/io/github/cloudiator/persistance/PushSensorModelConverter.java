package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.monitoring.domain.PushSensor;
import javax.annotation.Nullable;

public class PushSensorModelConverter implements OneWayConverter<PushSensorModel, PushSensor> {

  @Nullable
  @Override
  public PushSensor apply(@Nullable PushSensorModel pushSensorModel) {
    PushSensor result = new PushSensor().port(pushSensorModel.getPort());
    result.setType(PushSensor.class.getSimpleName());
    return result;
  }
}
