package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Entity
public class PropertyModel extends Model {

  @Column(nullable = false, name = "propertyKey")
  @Lob
  private String key;

  @Column(nullable = false)
  @Lob
  private String value;

  @ManyToOne(optional = false, targetEntity = SensorConfigurationModel.class)
  private SensorConfigurationModel sensorConfigurationModel;

  protected PropertyModel() {

  }

  public PropertyModel(SensorConfigurationModel sensorConfigurationModel, String key,
      String value) {
    checkNotNull(sensorConfigurationModel, "SensorConfigurationModel is null");
    checkNotNull(key, "key is null");
    checkNotNull(value, "value is null");
    checkArgument(!key.isEmpty(), "key is empty");
    checkArgument(!value.isEmpty(), "value is empty");
    this.sensorConfigurationModel = sensorConfigurationModel;
    this.key = key;
    this.value = value;
  }


  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public SensorConfigurationModel getSensorConfigurationModel() {
    return sensorConfigurationModel;
  }
}
