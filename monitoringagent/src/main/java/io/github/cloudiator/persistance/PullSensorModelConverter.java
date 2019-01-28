package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.rest.model.PullSensor;
import java.util.HashMap;
import javax.annotation.Nullable;

public class PullSensorModelConverter implements OneWayConverter<PullSensorModel, PullSensor> {

  private final IntervalModelConverter intervalModelConverter = new IntervalModelConverter();


  @Nullable
  @Override
  public PullSensor apply(@Nullable PullSensorModel pullSensorModel) {
    PullSensor result = new PullSensor().className(pullSensorModel.getClassName())
        .interval(intervalModelConverter.apply(pullSensorModel.getInterval()));
    result.setType(PullSensor.class.getSimpleName());
    HashMap config = new HashMap();
    config.putAll(pullSensorModel.getConfiguration());
    result.setConfiguration(config);
    return result;
  }

}
