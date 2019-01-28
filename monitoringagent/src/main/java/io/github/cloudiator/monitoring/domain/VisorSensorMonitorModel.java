package io.github.cloudiator.monitoring.domain;

import io.github.cloudiator.rest.model.DataSink;
import io.github.cloudiator.rest.model.Interval;
import java.util.List;
import java.util.Map;

public class VisorSensorMonitorModel extends VisorMonitorModel {

  String type;
  String metricName;
  String componentId;
  Map<String, String> monitorContext;
  List<DataSink> dataSinks;
  String sensorClassName;
  Interval interval;
  Map<String, String> sensorConfiguration;

  public VisorSensorMonitorModel() {
  }

  public VisorSensorMonitorModel type(String type) {
    this.type = type;
    return this;
  }

  public VisorSensorMonitorModel metricName(String metricName) {
    this.metricName = metricName;
    return this;
  }

  public VisorSensorMonitorModel componentId(String componentId) {
    this.componentId = componentId;
    return this;
  }

  public VisorSensorMonitorModel monitorContext(Map monitorContext) {
    this.monitorContext = monitorContext;
    return this;
  }

  public VisorSensorMonitorModel dataSinks(List dataSinks) {
    this.dataSinks = dataSinks;
    return this;
  }

  public VisorSensorMonitorModel sensorClassName(String sensorClassName) {
    this.sensorClassName = sensorClassName;
    return this;
  }

  public VisorSensorMonitorModel interval(Interval interval) {
    this.interval = interval;
    return this;
  }

  public VisorSensorMonitorModel sensorConfiguration(Map sensorConfiguration) {
    this.sensorConfiguration = sensorConfiguration;
    return this;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getMetricName() {
    return metricName;
  }

  public void setMetricName(String metricName) {
    this.metricName = metricName;
  }

  public String getComponentId() {
    return componentId;
  }

  public void setComponentId(String componentId) {
    this.componentId = componentId;
  }

  public Map<String, String> getMonitorContext() {
    return monitorContext;
  }

  public void setMonitorContext(Map<String, String> monitorContext) {
    this.monitorContext = monitorContext;
  }

  public List<DataSink> getDataSinks() {
    return dataSinks;
  }

  public void setDataSinks(List<DataSink> dataSinks) {
    this.dataSinks = dataSinks;
  }

  public String getSensorClassName() {
    return sensorClassName;
  }

  public void setSensorClassName(String sensorClassName) {
    this.sensorClassName = sensorClassName;
  }

  public Interval getInterval() {
    return interval;
  }

  public void setInterval(Interval interval) {
    this.interval = interval;
  }

  public Map<String, String> getSensorConfiguration() {
    return sensorConfiguration;
  }

  public void setSensorConfiguration(Map<String, String> sensorConfiguration) {
    this.sensorConfiguration = sensorConfiguration;
  }
}
