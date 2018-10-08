package io.github.cloudiator.monitoring.domain;

import com.fasterxml.jackson.annotation.JsonProperty;


import java.util.HashSet;
import java.util.Set;
import java.util.Objects;


/**
 * Key-Value configuration of the sensor
 */


public class SensorConfiguration {
  @JsonProperty("properties")

  private Set<Property> properties = null;

  public SensorConfiguration properties(Set<Property> properties) {
    this.properties = properties;
    return this;
  }

  public SensorConfiguration addPropertiesItem(Property propertiesItem) {
    if (this.properties == null) {
      this.properties = new HashSet<Property>();
    }
    this.properties.add(propertiesItem);
    return this;
  }

  /**
   * Array of configuration properties
   * @return properties
  **/


  public Set<Property> getProperties() {
    return properties;
  }

  public void setProperties(Set<Property> properties) {
    this.properties = properties;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SensorConfiguration sensorConfiguration = (SensorConfiguration) o;
    return Objects.equals(this.properties, sensorConfiguration.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(properties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SensorConfiguration {\n");

    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

