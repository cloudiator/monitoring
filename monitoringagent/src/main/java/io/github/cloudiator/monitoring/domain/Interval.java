package io.github.cloudiator.monitoring.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;


/**
 * A time interval consisting of unit and period
 */


public class Interval {

  /**
   * The unit of the interval
   */
  public enum UnitEnum {
    DAYS("DAYS"),

    HOURS("HOURS"),

    MICROSECONDS("MICROSECONDS"),

    MILLISECONDS("MILLISECONDS"),

    MINUTES("MINUTES"),

    NANOSECONDS("NANOSECONDS"),

    SECONDS("SECONDS");

    private String value;

    UnitEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static UnitEnum fromValue(String text) {
      for (UnitEnum b : UnitEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("unit")
  private UnitEnum unit = null;

  @JsonProperty("period")
  private Long period = null;

  public Interval unit(UnitEnum unit) {
    this.unit = unit;
    return this;
  }

  /**
   * Constuctor with Enum and period?
   **/

  public Interval(UnitEnum unit, Long period) {
    checkNotNull(unit);
    checkNotNull(period);
    this.unit = unit;
    this.period = period;

  }

  /**
   * The unit of the interval
   *
   * @return unit
   **/


  public UnitEnum getUnit() {
    return unit;
  }

  public void setUnit(UnitEnum unit) {
    this.unit = unit;
  }

  public Interval period(Long period) {
    this.period = period;
    return this;
  }

  /**
   * The period of the interval
   *
   * @return period
   **/


  public Long getPeriod() {
    return period;
  }

  public void setPeriod(Long period) {
    this.period = period;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Interval interval = (Interval) o;
    return Objects.equals(this.unit, interval.unit) &&
        Objects.equals(this.period, interval.period);
  }

  @Override
  public int hashCode() {
    return Objects.hash(unit, period);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Interval {\n");

    sb.append("    unit: ").append(toIndentedString(unit)).append("\n");
    sb.append("    period: ").append(toIndentedString(period)).append("\n");
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

