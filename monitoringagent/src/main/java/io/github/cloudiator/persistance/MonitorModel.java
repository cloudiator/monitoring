package io.github.cloudiator.persistance;


import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;


@Entity
public class MonitorModel extends Model {

  @Column(nullable = false, unique = true, updatable = false)
  private String metric;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private List<TargetModel> targets;

  @OneToOne(optional = false, orphanRemoval = true, cascade = CascadeType.ALL)
  private SensorModel sensor;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DataSinkModel> datasinks;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  private List<MTagModel> monitortags;

  @Column(unique = true)
  private String uuid;


  protected MonitorModel() {
  }

  public MonitorModel(String metric, List<TargetModel> targets, SensorModel sensor,
      List<DataSinkModel> datasinks, List<MTagModel> monitortags, String uuid) {
    checkNotNull(metric);
    checkNotNull(sensor);
    this.metric = metric;
    this.targets = targets;
    this.sensor = sensor;
    this.datasinks = datasinks;
    this.monitortags = monitortags;
    this.uuid = uuid;
  }

  public MonitorModel metric(String metric) {
    this.metric = metric;
    return this;
  }

  public String getUuid() {
    return uuid;
  }

  public String getMetric() {
    return metric;
  }

  public List<TargetModel> getTargets() {
    return targets;
  }

  public List<DataSinkModel> getDatasinks() {
    return datasinks;
  }

  public List<MTagModel> getMonitortags() {
    return monitortags;
  }

  public SensorModel getSensor() {
    return sensor;
  }

  public void setSensor(SensorModel sensor) {
    this.sensor = sensor;
  }


  public void addTarget(TargetModel targetModel) {
    if (targets == null) {
      targets = new ArrayList<TargetModel>();
    }
    if (!targets.contains(targetModel)) {
      targets.add(targetModel);
    }
  }


  public void addDataSink(DataSinkModel dataSinkModel) {
    if (datasinks == null) {
      datasinks = new ArrayList<DataSinkModel>();
    }
    if (!datasinks.contains(dataSinkModel)) {
      datasinks.add(dataSinkModel);
    }
  }


  public void addMonitoringTag(MTagModel tagModel) {
    if (monitortags == null) {
      monitortags = new ArrayList<MTagModel>();
    }
    if (!monitortags.contains(tagModel)) {
      monitortags.add(tagModel);
    }
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MonitorModel monitorModel = (MonitorModel) o;
    return Objects.equals(this.metric, monitorModel.metric) &&
        Objects.equals(this.sensor, monitorModel.sensor) &&
        Objects.equals(this.targets, monitorModel.targets) &&
        Objects.equals(this.datasinks, monitorModel.datasinks) &&
        Objects.equals(this.monitortags, monitorModel.monitortags);
  }

}
