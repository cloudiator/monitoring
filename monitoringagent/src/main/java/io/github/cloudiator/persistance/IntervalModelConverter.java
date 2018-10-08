package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.monitoring.domain.Interval;
import io.github.cloudiator.monitoring.domain.Interval.UnitEnum;

public class IntervalModelConverter implements OneWayConverter<IntervalModel, Interval> {


  @Override
  public Interval apply(IntervalModel intervalModel) {
    Interval interval = new Interval(UnitEnum.fromValue(intervalModel.getUnit().name()),
        intervalModel.getPeriod());
    return interval;
  }
}
