package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity
public class SensorConfigurationModel extends Model {

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "sensorConfigurationModel", orphanRemoval = true)
  private Set<PropertyModel> properties;

  @OneToOne
  private PullSensorModel pullSensorModel;

  protected SensorConfigurationModel() {
  }

  public PullSensorModel getPullSensorModel() {
    return pullSensorModel;
  }

  public Set<PropertyModel> getProperties() {
    return properties;
  }


  public void addProperty(PropertyModel propertyModel) {
    checkNotNull(propertyModel, "propertyModel is null");
    if (properties == null) {
      properties = new HashSet<>();
    }
    properties.add(propertyModel);
  }

  public void addProperty(String key, String value) {
    PropertyModel propertyModel = new PropertyModel(this, key, value);
    addProperty(propertyModel);
  }


}
