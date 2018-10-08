package io.github.cloudiator.persistance;

import de.uniulm.omi.cloudiator.util.OneWayConverter;
import io.github.cloudiator.monitoring.domain.Property;
import io.github.cloudiator.monitoring.domain.SensorConfiguration;
import javax.annotation.Nullable;

public class SensorConfigurationModelConverter implements
    OneWayConverter<SensorConfigurationModel, SensorConfiguration> {

  @Nullable
  @Override
  public SensorConfiguration apply(@Nullable SensorConfigurationModel sensorConfigurationModel) {
    SensorConfiguration result = new SensorConfiguration();
    for (PropertyModel propertyModel : sensorConfigurationModel.getProperties()) {
      result.addPropertiesItem(
          new Property().key(propertyModel.getKey()).value(propertyModel.getValue()));
    }

    return result;
  }
}
