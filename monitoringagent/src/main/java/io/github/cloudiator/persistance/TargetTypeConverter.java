package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.monitoring.models.TargetState;
import io.github.cloudiator.rest.model.MonitoringTarget.TypeEnum;
import javax.annotation.Nullable;

public class TargetTypeConverter implements OneWayConverter<TargetType, TypeEnum> {


  @Nullable
  @Override
  public TypeEnum apply(@Nullable TargetType targetType) {
    TypeEnum result;
    switch (targetType) {
      case TASK:
        result = TypeEnum.TASK;
        break;
      case JOB:
        result = TypeEnum.JOB;
        break;
      case PROCESS:
        result = TypeEnum.PROCESS;
        break;
      case NODE:
        result = TypeEnum.NODE;
        break;
      case CLOUD:
        result = TypeEnum.CLOUD;
        break;
      default:
        result = null;
    }
    return result;
  }
}
