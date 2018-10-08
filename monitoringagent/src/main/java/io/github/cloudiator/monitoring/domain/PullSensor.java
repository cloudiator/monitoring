package io.github.cloudiator.monitoring.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;


/**
 * PullSensor
 */

public class PullSensor extends Sensor {

  @JsonProperty("className")
  private String className = null;

  @JsonProperty("configuration")
  private SensorConfiguration configuration = null;

  @JsonProperty("interval")
  private Interval interval = null;

  public PullSensor className(String className) {
    this.className = className;
    return this;
  }

  /**
   * ClassName of the sensor
   *
   * @return className
   **/


  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public PullSensor configuration(SensorConfiguration configuration) {
    this.configuration = configuration;
    return this;
  }

  /**
   * Configuration of the sensor
   *
   * @return _configuration
   **/


  public SensorConfiguration getConfiguration() {
    return configuration;
  }

  public void setConfiguration(SensorConfiguration configuration) {
    this.configuration = configuration;
  }

  public PullSensor interval(Interval interval) {
    this.interval = interval;
    return this;
  }

  /**
   * The interval at which the sensor is executed
   *
   * @return interval
   **/


  public Interval getInterval() {
    return interval;
  }

  public void setInterval(Interval interval) {
    this.interval = interval;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PullSensor pullSensor = (PullSensor) o;
    return Objects.equals(this.className, pullSensor.className) &&
        Objects.equals(this.configuration, pullSensor.configuration) &&
        Objects.equals(this.interval, pullSensor.interval) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(className, configuration, interval, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PullSensor {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    className: ").append(toIndentedString(className)).append("\n");
    sb.append("    configuration: ").append(toIndentedString(configuration)).append("\n");
    sb.append("    interval: ").append(toIndentedString(interval)).append("\n");
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

