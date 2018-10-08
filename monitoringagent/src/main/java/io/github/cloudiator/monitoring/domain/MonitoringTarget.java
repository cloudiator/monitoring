package io.github.cloudiator.monitoring.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;


/**
 * MonitoringTarget
 */


public class MonitoringTarget   {
  /**
   * target to be monitored
   */
  public enum TypeEnum {
    JOB("JOB"),
    
    TASK("TASK"),
    
    PROCESS("PROCESS"),
    
    CLOUD("CLOUD"),
    
    NODE("NODE");

    private String value;

    TypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TypeEnum fromValue(String text) {
      for (TypeEnum b : TypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("type")
  private TypeEnum type = null;

  @JsonProperty("identifier")
  private String identifier = null;

  public MonitoringTarget type(TypeEnum type) {
    this.type = type;
    return this;
  }

  /**
   * target to be monitored
   * @return type
  **/



  public TypeEnum getType() {
    return type;
  }

  public void setType(TypeEnum type) {
    this.type = type;
  }

  public MonitoringTarget identifier(String identifier) {
    this.identifier = identifier;
    return this;
  }

  /**
   * identifier of a specific instance of the above type
   * @return identifier
  **/



  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MonitoringTarget monitoringTarget = (MonitoringTarget) o;
    return Objects.equals(this.type, monitoringTarget.type) &&
        Objects.equals(this.identifier, monitoringTarget.identifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, identifier);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MonitoringTarget {\n");

    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    identifier: ").append(toIndentedString(identifier)).append("\n");
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

