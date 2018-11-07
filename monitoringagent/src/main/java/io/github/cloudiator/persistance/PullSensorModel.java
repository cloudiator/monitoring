package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;


import java.util.Map;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
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

  @ElementCollection
  private Map<String, String> configuration;

  @ManyToOne(targetEntity = IntervalModel.class)
  private IntervalModel interval;

  protected PullSensorModel() {
  }

  public PullSensorModel(String className, java.util.Map configuration, IntervalModel interval) {
    checkNotNull(className);
    checkNotNull(configuration);
    checkNotNull(interval);
    this.className = className;
    this.interval = interval;
    this.configuration.putAll(configuration);
  }


  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public java.util.Map getConfiguration() {
    return configuration;
  }

  public void setConfiguration(java.util.Map configuration) {
    this.configuration = configuration;
  }

  public IntervalModel getInterval() {
    return interval;
  }

  public void setInterval(IntervalModel interval) {
    this.interval = interval;
  }

}
