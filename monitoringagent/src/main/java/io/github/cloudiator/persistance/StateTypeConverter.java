package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.monitoring.models.TargetState;
import javax.annotation.Nullable;

public class StateTypeConverter implements OneWayConverter<StateType, TargetState> {

  @Nullable
  @Override
  public TargetState apply(@Nullable StateType stateType) {
    TargetState result;
    switch (stateType) {
      case PENDING:
        result = TargetState.PENDING;
        break;
      case RUNNING:
        result = TargetState.RUNNING;
        break;
      case DELETED:
        result = TargetState.DELETED;
        break;
      case ERROR:
        result = TargetState.ERROR;
        break;
      default:
        result = null;
    }
    return result;
  }
}
