package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.monitoring.domain.MonitoringTarget;
import io.github.cloudiator.monitoring.domain.MonitoringTarget.TypeEnum;
import javax.annotation.Nullable;

public class TargetModelConverter implements OneWayConverter<TargetModel, MonitoringTarget> {

  @Nullable
  @Override
  public MonitoringTarget apply(@Nullable TargetModel targetModel) {
    MonitoringTarget result = new MonitoringTarget()
        .type(TypeEnum.fromValue(targetModel.getTargetType().name()))
        .identifier(targetModel.getIdentifier());
    return result;
  }
}
