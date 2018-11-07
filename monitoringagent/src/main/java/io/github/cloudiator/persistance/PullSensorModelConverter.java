package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.monitoring.domain.PullSensor;
import java.util.Map;
import javax.annotation.Nullable;

public class PullSensorModelConverter implements OneWayConverter<PullSensorModel, PullSensor> {

  private final IntervalModelConverter intervalModelConverter = new IntervalModelConverter();


  @Nullable
  @Override
  public PullSensor apply(@Nullable PullSensorModel pullSensorModel) {
    PullSensor result = new PullSensor().className(pullSensorModel.getClassName())
        .interval(intervalModelConverter.apply(pullSensorModel.getInterval()));
    result.setType(PullSensor.class.getSimpleName());
    result.setConfiguration(pullSensorModel.getConfiguration());
    return result;
  }

}
