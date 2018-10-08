package io.github.cloudiator.persistance;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;

@Entity(name = "PushSensorModel")
public class PushSensorModel extends SensorModel {

  @Column(nullable = false)
  @Lob
  private Integer port;

  protected PushSensorModel() {
  }

  public PushSensorModel(Integer port) {
    this.port = port;
  }

  public Integer getPort() {
    return port;
  }

  public void setPort(Integer port) {
    this.port = port;
  }
}
