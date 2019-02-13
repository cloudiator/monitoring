package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.rest.model.MonitoringTag;
import javax.annotation.Nullable;
@Deprecated
public class MTagModelConverter implements
    OneWayConverter<MTagModel, MonitoringTag> {

  @Nullable
  @Override
  public MonitoringTag apply(@Nullable MTagModel MTagModel) {
    if (MTagModel == null) {
      return null;
    }
    MonitoringTag result = new MonitoringTag()
        .key(MTagModel.getKey())
        .value(MTagModel.getValue());
    return result;
  }
}
