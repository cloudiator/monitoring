package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity(name = "PullSensorModel")
public class PullSensorModel extends SensorModel {

  @Column
  @Lob
  private String className;

  @OneToOne
  private SensorConfigurationModel sensorConfiguration;

  @ManyToOne(targetEntity = IntervalModel.class)
  private IntervalModel interval;

  protected PullSensorModel() {
  }

  public PullSensorModel(String className, SensorConfigurationModel sensorConfiguration,
      IntervalModel interval) {
    checkNotNull(className);
    checkNotNull(sensorConfiguration);
    checkNotNull(interval);
    this.className = className;
    this.sensorConfiguration = sensorConfiguration;
    this.interval = interval;

  }


  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public SensorConfigurationModel getSensorConfiguration() {
    return sensorConfiguration;
  }

  public void setSensorConfiguration(
      SensorConfigurationModel sensorConfiguration) {
    this.sensorConfiguration = sensorConfiguration;
  }

  public IntervalModel getInterval() {
    return interval;
  }

  public void setInterval(IntervalModel interval) {
    this.interval = interval;
  }

}
