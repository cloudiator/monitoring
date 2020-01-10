package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.rest.model.Interval;
import io.github.cloudiator.rest.model.TimeUnit;
import javax.annotation.Nullable;

public class IntervalModelConverter implements OneWayConverter<IntervalModel, Interval> {


  @Nullable
  @Override
  public Interval apply(@Nullable IntervalModel intervalModel) {
    if (intervalModel == null) {
      return null;
    }
    Interval interval = new Interval()
        .unit(TimeUnit.valueOf(intervalModel.getUnit().name()))
        .period(intervalModel.getPeriod());
    return interval;
  }
}
