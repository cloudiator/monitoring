package io.github.cloudiator.monitoring.domain;

import io.github.cloudiator.rest.model.DataSink;
import java.util.List;
import java.util.Map;

public class VisorPushMonitorModel extends VisorMonitorModel {

  private String type;
  private String metricName;
  private String componentId;
  private Map<String, String> monitorContext;
  private List<DataSink> dataSinks;
  private Integer port;

  public VisorPushMonitorModel() {
  }

  public VisorPushMonitorModel type(String type) {
    this.type = type;
    return this;
  }

  public VisorPushMonitorModel metricName(String metricName) {
    this.metricName = metricName;
    return this;
  }

  public VisorPushMonitorModel componentId(String componentId) {
    this.componentId = componentId;
    return this;
  }

  public VisorPushMonitorModel monitorContext(Map monitorContext) {
    this.monitorContext = monitorContext;
    return this;
  }

  public VisorPushMonitorModel dataSinks(List dataSinks) {
    this.dataSinks = dataSinks;
    return this;
  }

  public VisorPushMonitorModel port(Integer port) {
    this.port = port;
    return this;
  }

  public String gettype() {
    return type;
  }

  public void settype(String type) {
    this.type = type;
  }

  public String getmetricName() {
    return metricName;
  }

  public void setmetricName(String metricName) {
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

  public List<DataSink> getdataSinks() {
    return dataSinks;
  }

  public void setdataSinks(List<DataSink> dataSinks) {
    this.dataSinks = dataSinks;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }
}
