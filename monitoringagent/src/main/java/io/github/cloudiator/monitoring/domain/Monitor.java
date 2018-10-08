package io.github.cloudiator.monitoring.domain;



import java.util.HashSet;
import java.util.Set;
import java.util.Objects;


/**
 * Monitor
 */


public class Monitor {


  private final String metric;
  private Set<MonitoringTarget> targets = null;
  private Sensor sensor = null;
  private Set<DataSink> sinks = null;
  private Set<MonitoringTag> tags = null;


  public Monitor(String metric) {
    this.metric = metric;
  }


  public String getMetric() {
    return metric;
  }

  public Monitor addTargetsItem(MonitoringTarget targetsItem) {
    if (this.targets == null) {
      this.targets = new HashSet<MonitoringTarget>();
    }
    this.targets.add(targetsItem);
    return this;
  }

  public Set<MonitoringTarget> getTargets() {
    return targets;
  }

  public void setTargets(Set<MonitoringTarget> targets) {
    this.targets = targets;
  }

  public Sensor getSensor() {
    return sensor;
  }

  public void setSensor(Sensor sensor) {
    this.sensor = sensor;
  }

  public Monitor addSinksItem(DataSink sinksItem) {
    if (this.sinks == null) {
      this.sinks = new HashSet<DataSink>();
    }
    this.sinks.add(sinksItem);
    return this;
  }

  public Set<DataSink> getSinks() {
    return sinks;
  }

  public void setSinks(Set<DataSink> sinks) {
    this.sinks = sinks;
  }

  public Monitor tags(Set<MonitoringTag> tags) {
    this.tags = tags;
    return this;
  }

  public Monitor addTagsItem(MonitoringTag tagsItem) {
    if (this.tags == null) {
      this.tags = new HashSet<MonitoringTag>();
    }
    this.tags.add(tagsItem);
    return this;
  }

  public Set<MonitoringTag> getTags() {
    return tags;
  }

  public void setTags(Set<MonitoringTag> tags) {
    this.tags = tags;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Monitor monitor = (Monitor) o;
    return Objects.equals(this.metric, monitor.metric) &&
        Objects.equals(this.targets, monitor.targets) &&
        Objects.equals(this.sensor, monitor.sensor) &&
        Objects.equals(this.sinks, monitor.sinks) &&
        Objects.equals(this.tags, monitor.tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(metric, targets, sensor, sinks, tags);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Monitor {\n");

    sb.append("    metric: ").append(toIndentedString(metric)).append("\n");
    sb.append("    targets: ").append(toIndentedString(targets)).append("\n");
    sb.append("    sensor: ").append(toIndentedString(sensor)).append("\n");
    sb.append("    sinks: ").append(toIndentedString(sinks)).append("\n");
    sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
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

