package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.monitoring.domain.DataSink;
import io.github.cloudiator.monitoring.domain.DataSinkConfiguration;
import javax.annotation.Nullable;

public class DataSinkModelConverter implements OneWayConverter<DataSinkModel, DataSink> {

  @Nullable
  @Override
  public DataSink apply(@Nullable DataSinkModel dataSinkModel) {
    DataSink result = new DataSink()
        .type(DataSink.TypeEnum.fromValue(dataSinkModel.getType().name()));

    for (DataSinkConfigurationModel configurationModel : dataSinkModel.getDataSinkConfiguration()) {
      result.addConfigurationItem(
          new DataSinkConfiguration(configurationModel.getKey(), configurationModel.getValue()));
    }

    return result;
  }
}
