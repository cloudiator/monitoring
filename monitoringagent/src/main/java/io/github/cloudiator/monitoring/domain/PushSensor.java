package io.github.cloudiator.monitoring.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;


/**
 * PushSensor
 */

public class PushSensor extends Sensor  {
  @JsonProperty("port")
  private Integer port = null;

  public PushSensor port(Integer port) {
    this.port = port;
    return this;
  }


  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PushSensor pushSensor = (PushSensor) o;
    return Objects.equals(this.port, pushSensor.port) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(port, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PushSensor {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    port: ").append(toIndentedString(port)).append("\n");
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

