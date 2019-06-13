package io.github.cloudiator.persistance;


import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;


@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MonitorModel extends BaseModel {

  @Column(nullable = false, updatable = false)
  private String metric;

  @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY)
  @Cascade(org.hibernate.annotations.CascadeType.DELETE)
  private List<TargetModel> targets;

  @OneToOne(orphanRemoval = true)
  @Cascade(org.hibernate.annotations.CascadeType.DELETE)
  private SensorModel sensor;

  @OneToMany(orphanRemoval = true)
  @Cascade(org.hibernate.annotations.CascadeType.DELETE)
  private List<DataSinkModel> datasinks;

  @ElementCollection
  private Map<String, String> monitortags;

  @Column(unique = false)
  private String uuid;

  @Column(nullable = false, updatable = false)
  private String owner;




  protected MonitorModel() {
  }

  public MonitorModel(String metric, List<TargetModel> targets, SensorModel sensor,
      List<DataSinkModel> datasinks, Map monitorTags, String uuid, String userid) {
    checkNotNull(metric);
    checkNotNull(sensor);
    this.metric = metric;
    this.targets = targets;
    this.sensor = sensor;
    this.datasinks = datasinks;
    this.monitortags = monitorTags;
    this.uuid = uuid;
    this.owner = userid;
  }

  public MonitorModel metric(String metric) {
    this.metric = metric;
    return this;
  }

  public String getUuid() {
    return uuid;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getMetric() {
    return metric;
  }

  public void setMetric(String metric) {
    this.metric = metric;
  }

  public List<TargetModel> getTargets() {
    return targets;
  }

  public List<DataSinkModel> getDatasinks() {
    return datasinks;
  }

  public Map<String, String> getMonitortags() {
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


  public void setMonitoringTags(Map monitorTag) {
    if (this.monitortags == null) {
      this.monitortags = new HashMap<>();
    }
    this.monitortags.putAll(monitorTag);
  }

  public void addTag(String value1, String value2) {
    if (this.monitortags == null) {
      this.monitortags = new HashMap<>();
    }
    this.monitortags.put(value1, value2);
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

  public void updateTargets(List<TargetModel> targetsToUpdate){
    //remove old targets
    for (TargetModel actualTarget:this.targets) {
      if (!targetsToUpdate.contains(actualTarget)){
        this.targets.remove(actualTarget);
      }
    }
    //add new targets
    for (TargetModel updateTarget:targetsToUpdate) {
      if (!this.targets.contains(updateTarget)){
        this.targets.add(updateTarget);
      }
    }
  }

}
