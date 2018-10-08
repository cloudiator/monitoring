package io.github.cloudiator.monitoring.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;


/**
 * DataSinkConfiguration
 */


public class DataSinkConfiguration {

  @JsonProperty("key")
  private String key = null;

  @JsonProperty("value")
  private String value = null;

  public DataSinkConfiguration key(String key) {
    this.key = key;
    return this;
  }

  public DataSinkConfiguration(String key, String value) {
    this.key = key;
    this.value = value;
  }


  /**
   * Get key
   *
   * @return key
   **/


  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public DataSinkConfiguration value(String value) {
    this.value = value;
    return this;
  }

  /**
   * Get value
   *
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
    DataSinkConfiguration dataSinkConfiguration = (DataSinkConfiguration) o;
    return Objects.equals(this.key, dataSinkConfiguration.key) &&
        Objects.equals(this.value, dataSinkConfiguration.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DataSinkConfiguration {\n");

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

