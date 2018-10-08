package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.hibernate.collection.internal.PersistentSet;

@Entity
public class MonitorModel extends Model {

  @Column(nullable = false, unique = true, updatable = false)
  private String metric;

  @ManyToMany
  @JoinTable(name = "monitor_has_targets")
  private Set<TargetModel> targets;

  @OneToOne(orphanRemoval = true)
  private SensorModel sensor;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "monitor")
  private Set<DataSinkModel> datasinks;

  @ManyToMany
  @JoinTable(name = "monitor_has_tags")
  private Set<MonitoringTagModel> monitortags;


  protected MonitorModel() {
  }

  public MonitorModel(String metric, Set<TargetModel> targets, SensorModel sensor,
      Set<DataSinkModel> datasinks, Set<MonitoringTagModel> monitortags) {
    checkNotNull(metric);
    checkNotNull(sensor);
    this.metric = metric;
    this.targets = targets;
    this.sensor = sensor;
    this.datasinks = datasinks;
    this.monitortags = monitortags;
  }


  public String getMetric() {
    return metric;
  }

  public Set<TargetModel> getTargets() {
    return targets;
  }

  public Set<DataSinkModel> getDatasinks() {
    return datasinks;
  }

  public Set<MonitoringTagModel> getMonitortags() {
    return monitortags;
  }


  public void addTarget(TargetModel targetModel) {
    checkNotNull(targetModel, "TargetModel is null");
    if (targets == null) {
      targets = new HashSet<TargetModel>();
    }
    if (!targets.contains(targetModel)) {
      targets.add(targetModel);
    }
  }


  public void addDataSink(DataSinkModel dataSinkModel) {
    checkNotNull(dataSinkModel, "DataSinkModel is null");
    if (datasinks == null) {
      datasinks = new HashSet<DataSinkModel>();
    }
    if (!datasinks.contains(dataSinkModel)) {
      datasinks.add(dataSinkModel);
    }
  }

  public void addDataSink(DataSinkType dataSinkType,
      Set<DataSinkConfigurationModel> dataSinkConfigurationModels) {
    DataSinkModel dataSinkModel = new DataSinkModel(this, dataSinkType,
        dataSinkConfigurationModels);
    addDataSink(dataSinkModel);
  }

  public void addMonitoringTag(MonitoringTagModel tagModel) {
    checkNotNull(tagModel, "MonitoringTagModel is null");
    if (monitortags == null) {
      monitortags = new HashSet<MonitoringTagModel>();
    }
    if (!monitortags.contains(tagModel)) {
      monitortags.add(tagModel);
    }
  }


  public SensorModel getSensor() {
    return sensor;
  }

  public void setSensor(SensorModel sensor) {
    this.sensor = sensor;
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
        Objects.equals(this.datasinks, monitorModel.datasinks);
  }

}
