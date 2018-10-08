package io.github.cloudiator.monitoring.converter;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.monitoring.domain.DataSink;
import io.github.cloudiator.monitoring.domain.DataSink.TypeEnum;
import io.github.cloudiator.monitoring.domain.DataSinkConfiguration;
import org.cloudiator.messages.entities.MonitorEntities;
import org.cloudiator.messages.entities.MonitorEntities.SinkConfiguration;
import org.cloudiator.messages.entities.MonitorEntities.SinkType;

public class DataSinkConverter implements TwoWayConverter<DataSink, MonitorEntities.Sink> {

  @Override
  public DataSink applyBack(MonitorEntities.Sink kafkaDataSink) {
    DataSink result = new DataSink().type(TypeEnum.valueOf(kafkaDataSink.getType().name()));
    for (MonitorEntities.SinkConfiguration sinkConfig : kafkaDataSink.getConfigurationList()) {
      result.addConfigurationItem(
          new DataSinkConfiguration(sinkConfig.getKey(), sinkConfig.getValue()));
    }
    return result;
  }

  @Override
  public MonitorEntities.Sink apply(DataSink domainDataSink) {
    MonitorEntities.Sink.Builder result = MonitorEntities.Sink.newBuilder()
        .setType(SinkType.valueOf(domainDataSink.getType().name()));
    for (DataSinkConfiguration dataSinkconfig : domainDataSink.getConfiguration()) {
      result.addConfiguration(SinkConfiguration.newBuilder()
          .setKey(dataSinkconfig.getKey()).setValue(dataSinkconfig.getValue()).build());
    }
    return result.build();
  }
}
