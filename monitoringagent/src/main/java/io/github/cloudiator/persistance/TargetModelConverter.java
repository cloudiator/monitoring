package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.rest.model.MonitoringTarget.TypeEnum;
import javax.annotation.Nullable;


public class TargetModelConverter implements OneWayConverter<TargetModel, MonitoringTarget> {

  @Nullable
  @Override
  public MonitoringTarget apply(@Nullable TargetModel targetModel) {
    final MonitoringTarget result = new MonitoringTarget()
        .type(TypeEnum.valueOf(targetModel.getTargetType().name()))
        .identifier(targetModel.getIdentifier());
    return result;
  }
}
