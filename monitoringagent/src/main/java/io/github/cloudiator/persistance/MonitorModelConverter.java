package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.MonitoringTag;
import javax.annotation.Nullable;

public class MonitorModelConverter implements OneWayConverter<MonitorModel, Monitor> {

  private final DataSinkModelConverter dataSinkModelConverter = new DataSinkModelConverter();
  private final TargetModelConverter targetModelConverter = new TargetModelConverter();
  private final SensorModelConverter sensorModelConverter = new SensorModelConverter();

  @Nullable
  @Override
  public Monitor apply(@Nullable MonitorModel monitorModel) {
    //Metric
    Monitor result = new Monitor()
        .metric(monitorModel.getMetric());
    //Target
    for (TargetModel targetModel : monitorModel.getTargets()) {
      result.addTargetsItem(targetModelConverter.apply(targetModel));
    }
    //Sensor
    result.setSensor(sensorModelConverter.apply(monitorModel.getSensor()));
    //DataSink
    for (DataSinkModel dataSinkModel : monitorModel.getDatasinks()) {
      result.addSinksItem(dataSinkModelConverter.apply(dataSinkModel));
    }
    //MonitoringTags
    for (MTagModel tagModel : monitorModel.getMonitortags()) {
      result.addTagsItem(new MonitoringTag().key(tagModel.getKey()).value(tagModel.getValue()));
    }

    return result;
  }
}
