package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
class PullSensorModel extends SensorModel {

  @Column
  private String className;

  @ElementCollection
  private Map<String, String> configuration;

  @OneToOne(orphanRemoval = true)
  @Cascade(CascadeType.DELETE)
  private IntervalModel interval;

  protected PullSensorModel() {
  }

  public PullSensorModel(String className, java.util.Map configuration, IntervalModel interval) {
    this.className = className;
    this.interval = interval;
    //this.configuration = new HashMap();
    this.configuration = configuration;
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
    if (this.configuration == null) {
      this.configuration = new HashMap();
    }
    this.configuration.putAll(configuration);
  }

  public IntervalModel getInterval() {
    return interval;
  }

  public void setInterval(IntervalModel interval) {
    this.interval = interval;
  }

}
