package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.rest.model.MonitoringTag;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

public class MonitorModelConverter implements OneWayConverter<MonitorModel, DomainMonitorModel> {

  public static final MonitorModelConverter INSTANCE = new MonitorModelConverter();

  private final DataSinkModelConverter dataSinkModelConverter = new DataSinkModelConverter();
  private final TargetModelConverter targetModelConverter = new TargetModelConverter();
  private final SensorModelConverter sensorModelConverter = new SensorModelConverter();

  @Nullable
  @Override
  public DomainMonitorModel apply(@Nullable MonitorModel monitorModel) {
    //Metric
    DomainMonitorModel result = new DomainMonitorModel()
        .metric(monitorModel.getMetric().split("[+++]", 2)[0]);
    //Target
    for (TargetModel targetModel : monitorModel.getTargets()) {
      result.addTargetsItem(targetModelConverter.apply(targetModel));
    }
    System.out.println("targets done");
    //Sensor
    result.setSensor(sensorModelConverter.apply(monitorModel.getSensor()));
    //DataSink
    for (DataSinkModel dataSinkModel : monitorModel.getDatasinks()) {
      result.addSinksItem(dataSinkModelConverter.apply(dataSinkModel));
    }
    //MonitoringTags
    Map tags = new HashMap();
    if (!monitorModel.getMonitortags().isEmpty()) {
      tags.putAll(monitorModel.getMonitortags());
    }
    result.setTags(tags);
    result.setUuid(monitorModel.getUuid());
    System.out.println(result.toString());

    return result;
  }
}
