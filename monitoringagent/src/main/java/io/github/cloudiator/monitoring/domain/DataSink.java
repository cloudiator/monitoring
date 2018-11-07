package io.github.cloudiator.monitoring.domain;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;


/**
 * Data Sink where the monitoring data will be reported to.
 */


public class DataSink {

  /**
   * Gets or Sets type
   */
  public enum TypeEnum {
    KAIROS_DB("KAIROS_DB"),

    INFLUX("INFLUX");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }


    public static TypeEnum fromValue(String text) {
      for (TypeEnum b : TypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }


  private TypeEnum type = null;

  private java.util.Map configuration = null;



  public DataSink type(TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * Get type
   *
   * @return type
   **/


  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public DataSink configuration(java.util.Map configuration) {
    this.configuration = configuration;
    return this;
  }


  /**
   * Get _configuration
   *
   * @return _configuration
   **/


  public java.util.Map getConfiguration() {
    return configuration;
  }

  public void setConfiguration(java.util.Map configuration) {
    this.configuration = configuration;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DataSink dataSink = (DataSink) o;
    return Objects.equals(this.type, dataSink.type) &&
        Objects.equals(this.configuration, dataSink.configuration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, configuration);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DataSink {\n");

    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    configuration: ").append(toIndentedString(configuration)).append("\n");
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

