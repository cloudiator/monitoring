package io.github.cloudiator.monitoring.converter;

import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.rest.model.PushSensor;
import org.cloudiator.messages.entities.MonitorEntities;

public class PushSensorConverter implements
    TwoWayConverter<PushSensor, MonitorEntities.PushSensor> {

  @Override
  public PushSensor applyBack(MonitorEntities.PushSensor kafkaPushSensor) {
    PushSensor result = new PushSensor().port(kafkaPushSensor.getPort());
    result.setType(result.getClass().getSimpleName());
    return result;
  }

  @Override
  public MonitorEntities.PushSensor apply(PushSensor domainPushSensor) {
    MonitorEntities.PushSensor.Builder result = MonitorEntities.PushSensor.newBuilder()
        .setPort(domainPushSensor.getPort());
    return result.build();
  }


}
