package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.rest.model.DataSink;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.rest.model.Sensor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

public class MonitorModelConverter implements OneWayConverter<MonitorModel, DomainMonitorModel> {

  public static final MonitorModelConverter INSTANCE = new MonitorModelConverter();

  private final DataSinkModelConverter dataSinkModelConverter = new DataSinkModelConverter();
  private final TargetModelConverter targetModelConverter = new TargetModelConverter();
  private final SensorModelConverter sensorModelConverter = new SensorModelConverter();
  private final StateTypeConverter stateTypeConverter = new StateTypeConverter();
  private final TargetTypeConverter targetTypeConverter = new TargetTypeConverter();

  @Nullable
  @Override
  public DomainMonitorModel apply(@Nullable MonitorModel monitorModel) {

    //Target
    List<MonitoringTarget> monitoringTargets = new ArrayList<>();
    for (TargetModel targetModel : monitorModel.getTargets()) {
      monitoringTargets.add(targetModelConverter.apply(targetModel));
    }

    //Sensor
    Sensor sensor = sensorModelConverter.apply(monitorModel.getSensor());

    //DataSink
    List<DataSink> dataSinks = new ArrayList<>();
    for (DataSinkModel dataSinkModel : monitorModel.getDatasinks()) {
      dataSinks.add(dataSinkModelConverter.apply(dataSinkModel));
    }

    //MonitoringTags
    Map tags = new HashMap();
    if (!monitorModel.getMonitortags().isEmpty()) {
      tags.putAll(monitorModel.getMonitortags());
    }

    //DomainMonitorModel
    DomainMonitorModel result = new DomainMonitorModel(monitorModel.getMetric(),
        targetTypeConverter.apply(monitorModel.getOwnTargetType()), monitorModel.getOwnTargetId(),
        monitoringTargets, sensor,
        dataSinks, tags);
    result.setOwnTargetState(stateTypeConverter.apply(monitorModel.getOwnTargetState()));
    result.setUuid(monitorModel.getVisorUuid());
    result.setOwner(monitorModel.getOwner());

    return result;
  }
}
