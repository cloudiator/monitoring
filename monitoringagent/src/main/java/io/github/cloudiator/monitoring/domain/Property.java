package io.github.cloudiator.monitoring.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;


/**
 * A key-value pair
 */


public class Property {
  @JsonProperty("key")
  private String key = null;

  @JsonProperty("value")
  private String value = null;

  public Property key(String key) {
    this.key = key;
    return this;
  }

  /**
   * Key of the property
   * @return key
  **/


  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public Property value(String value) {
    this.value = value;
    return this;
  }

  /**
   * Value of the property
   * @return value
  **/


  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Property property = (Property) o;
    return Objects.equals(this.key, property.key) &&
        Objects.equals(this.value, property.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Property {\n");

    sb.append("    key: ").append(toIndentedString(key)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

