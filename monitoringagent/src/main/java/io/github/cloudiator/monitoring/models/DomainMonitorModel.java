package io.github.cloudiator.monitoring.models;

import io.github.cloudiator.rest.model.DataSink;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.MonitoringTag;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.rest.model.Sensor;
import java.util.List;
import java.util.Objects;

public class DomainMonitorModel extends Monitor {

  private String uuid;

  public DomainMonitorModel() {
    super();
  }

  public DomainMonitorModel(String metric, List<MonitoringTarget> targets, Sensor sensor,
      List<DataSink> sinks, List<MonitoringTag> tags, String uuid) {
    super.metric(metric);
    super.targets(targets);
    super.sensor(sensor);
    super.sinks(sinks);
    super.tags(tags);
    this.uuid = uuid;
  }

  public DomainMonitorModel(Monitor monitor) {
    super();
    this.uuid = "0";
  }

  public DomainMonitorModel metric(String metric) {
    super.metric(metric);
    return this;
  }

  public DomainMonitorModel targets(List<MonitoringTarget> targets) {
    super.targets(targets);
    return this;
  }

  public DomainMonitorModel sensor(Sensor sensor) {
    super.sensor(sensor);
    return this;
  }

  public DomainMonitorModel sinks(List<DataSink> sinks) {
    super.sinks(sinks);
    return this;
  }


  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getUuid() {
    return uuid;
  }


  public String getMetric() {
    return super.getMetric();
  }

  public void setMetric(String metric) {
    super.setMetric(metric);
  }


  public DomainMonitorModel addTargetsItem(MonitoringTarget targetsItem) {
    super.addTargetsItem(targetsItem);
    return this;
  }

  public List<MonitoringTarget> getTargets() {
    return super.getTargets();
  }

  public void setTargets(List<MonitoringTarget> targets) {
    super.setTargets(targets);
  }

  public Sensor getSensor() {
    return super.getSensor();
  }

  public void setSensor(Sensor sensor) {
    super.setSensor(sensor);
  }


  public DomainMonitorModel addSinksItem(DataSink sinksItem) {
    super.addSinksItem(sinksItem);
    return this;
  }

  public List<DataSink> getSinks() {
    return super.getSinks();
  }

  public void setSinks(List<DataSink> sinks) {
    super.setSinks(sinks);
  }

  public DomainMonitorModel tags(List<MonitoringTag> tags) {
    super.tags(tags);
    return this;
  }

  public Monitor addTagsItem(MonitoringTag tagsItem) {
    super.addTagsItem(tagsItem);
    return this;
  }

  public List<MonitoringTag> getTags() {
    return super.getTags();
  }

  public void setTags(List<MonitoringTag> tags) {
    super.setTags(tags);
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o != null && this.getClass() == o.getClass()) {
      DomainMonitorModel monitor = (DomainMonitorModel) o;
      return Objects.equals(this.getMetric(), monitor.getMetric()) && Objects
          .equals(this.getTargets(), monitor.getTargets()) && Objects
          .equals(this.getSensor(), monitor.getSensor())
          && Objects.equals(this.getSinks(), monitor.getSinks()) && Objects
          .equals(this.getTags(), monitor.getTags())
          && Objects.equals(this.uuid, monitor.uuid);
    } else {
      return false;
    }
  }

  public int hashCode() {
    return Objects
        .hash(new Object[]{this.getMetric(), this.getTargets(), this.getSensor(), this.getSinks(),
            this.getTags(), this.uuid});
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DomainMonitormodel {\n");
    sb.append("    metric: ").append(this.toIndentedString(this.getMetric())).append("\n");
    sb.append("    targets: ").append(this.toIndentedString(this.getTargets())).append("\n");
    sb.append("    sensor: ").append(this.toIndentedString(this.getSensor())).append("\n");
    sb.append("    sinks: ").append(this.toIndentedString(this.getSinks())).append("\n");
    sb.append("    tags: ").append(this.toIndentedString(this.getTags())).append("\n");
    sb.append("    uuid: ").append(this.toIndentedString(this.uuid)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  private String toIndentedString(Object o) {
    return o == null ? "null" : o.toString().replace("\n", "\n    ");
  }
}



