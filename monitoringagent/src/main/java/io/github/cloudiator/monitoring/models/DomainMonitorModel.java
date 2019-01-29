package io.github.cloudiator.monitoring.models;

import io.github.cloudiator.rest.model.Monitor;

public class DomainMonitorModel extends Monitor {

  private String uuid;


  public DomainMonitorModel() {

  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getUuid() {
    return uuid;
  }

}
