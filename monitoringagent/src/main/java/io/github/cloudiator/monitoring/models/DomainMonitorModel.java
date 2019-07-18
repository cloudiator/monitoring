package io.github.cloudiator.monitoring.models;


import io.github.cloudiator.rest.model.DataSink;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.rest.model.MonitoringTarget.TypeEnum;
import io.github.cloudiator.rest.model.Sensor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class DomainMonitorModel extends Monitor {

  private String uuid;

  private TypeEnum ownTargetType;

  private String ownTargetId;

  private TargetState ownTargetState;

  public DomainMonitorModel() {
    super();
  }

  public DomainMonitorModel(String metric, TypeEnum ownTargetType, String ownTargetId,
      List<MonitoringTarget> targets, Sensor sensor,
      List<DataSink> sinks, Map tags) {
    super.metric(metric);
    this.ownTargetType = ownTargetType;
    this.ownTargetId = ownTargetId;
    this.ownTargetState = TargetState.PENDING;
    super.targets(targets);
    super.sensor(sensor);
    super.sinks(sinks);
    super.tags(tags);
    this.uuid = "";
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getUuid() {
    return uuid;
  }

  public String getOwnTargetId() {
    return ownTargetId;
  }

  public void setOwnTargetId(String ownTargetId) {
    this.ownTargetId = ownTargetId;
  }

  public TypeEnum getOwnTargetType() {
    return ownTargetType;
  }

  public void setOwnTargetType(TypeEnum ownTargetType) {
    this.ownTargetType = ownTargetType;
  }

  public TargetState getOwnTargetState() {
    return ownTargetState;
  }

  public void setOwnTargetState(TargetState ownTargetState) {
    this.ownTargetState = ownTargetState;
  }

  public String getMetric() {
    String result = super.getMetric();
    return result;
  }

  public void setMetric(String metric) {
    super.setMetric(metric);
  }


  public DomainMonitorModel addTargetsItem(MonitoringTarget targetsItem) {
    List<MonitoringTarget> monitoringTargetList = this.getTargets();
    if (monitoringTargetList == null) {
      monitoringTargetList = new ArrayList<>();
    }
    monitoringTargetList.add(targetsItem);
    super.setTargets(monitoringTargetList);
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

  public List<DataSink> getSinks() {
    return super.getSinks();
  }

  public void setSinks(List<DataSink> sinks) {
    super.setSinks(sinks);
  }

  public DomainMonitorModel tags(Map tags) {
    super.tags(tags);
    return this;
  }

  public Map<String, String> getTags() {
    return super.getTags();
  }

  public void setTags(Map tags) {
    super.setTags(tags);
  }

  public void addTagItem(String value1, String value2) {
    Map<String, String> tagmap = this.getTags();
    if (tagmap == null) {
      tagmap = new HashMap<>();
    }
    tagmap.put(value1, value2);
    this.setTags(tagmap);
  }


  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (o != null && this.getClass() == o.getClass()) {
      DomainMonitorModel monitor = (DomainMonitorModel) o;
      return Objects.equals(this.getMetric(), monitor.getMetric()) &&
          Objects.equals(this.getOwnTargetType(), monitor.getOwnTargetType()) &&
          Objects.equals(this.getOwnTargetId(), monitor.getOwnTargetId()) &&
          Objects.equals(this.getOwnTargetState(), monitor.getOwnTargetState()) &&
          Objects.equals(this.getTargets(), monitor.getTargets()) &&
          Objects.equals(this.getSensor(), monitor.getSensor()) &&
          Objects.equals(this.getSinks(), monitor.getSinks()) &&
          Objects.equals(this.getTags(), monitor.getTags()) &&
          Objects.equals(this.uuid, monitor.uuid);
    } else {
      return false;
    }
  }

  public int hashCode() {
    return Objects
        .hash(new Object[]{this.getMetric(), this.getOwnTargetType(), this.getOwnTargetId(),
            this.getOwnTargetState(), this.getTargets(), this.getSensor(), this.getSinks(),
            this.getTags(), this.uuid});
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DomainMonitormodel {\n");
    sb.append("    metric: ").append(this.toIndentedString(this.getMetric())).append("\n");
    sb.append("    ownTargetType: ").append(this.toIndentedString(this.getOwnTargetType()))
        .append("\n");
    sb.append("    ownTargetId: ").append(this.toIndentedString(this.getOwnTargetId()))
        .append("\n");
    sb.append("    ownTargetState: ").append(this.toIndentedString(this.getOwnTargetState()))
        .append("\n");
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



