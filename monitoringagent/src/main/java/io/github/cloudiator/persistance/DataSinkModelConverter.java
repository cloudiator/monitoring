package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.rest.model.DataSink;
import io.github.cloudiator.rest.model.DataSink.TypeEnum;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class DataSinkModelConverter implements OneWayConverter<DataSinkModel, DataSink> {

  @Nullable
  @Override
  public DataSink apply(@Nullable DataSinkModel dataSinkModel) {
    if (dataSinkModel == null) {
      return null;
    }
    Map config = new HashMap();
    config.putAll(dataSinkModel.getConfiguration());
    DataSink result = new DataSink()
        .type(dataSinkModel.getSinkType())
        ._configuration(config);

    return result;
  }
}
