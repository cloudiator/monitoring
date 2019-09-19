package io.github.cloudiator.persistance;


import static com.google.common.base.Preconditions.checkNotNull;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;


@Entity
public class MonitorModel extends Model {

  @Column(nullable = false, updatable = false)
  private String metric;

  @Column(nullable = false, updatable = false)
  private TargetType ownTargetType;

  @Column(nullable = false, updatable = false)
  private String ownTargetId;

  @Column(nullable = true, updatable = true)
  private StateType ownTargetState;

  @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY)
  @Cascade(CascadeType.DELETE)
  private List<TargetModel> targets;

  @OneToOne(orphanRemoval = true)
  @Cascade(CascadeType.DELETE)
  private SensorModel sensor;

  @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY)
  @Cascade(CascadeType.DELETE)
  private List<DataSinkModel> datasinks;

  @ElementCollection
  private Map<String, String> monitortags;

  @Column(unique = false)
  private String visorUuid;

  @Column(nullable = false, updatable = false)
  private String owner;


  protected MonitorModel() {
  }

  public MonitorModel(String metric, TargetType ownTargetType, String ownTargetId, List<TargetModel> targets, SensorModel sensor,
      List<DataSinkModel> datasinks, Map monitorTags, String userid) {
    checkNotNull(metric);
    checkNotNull(sensor);
    this.metric = metric;
    this.ownTargetType = ownTargetType;
    this.ownTargetId = ownTargetId;
    this.targets = targets;
    this.sensor = sensor;
    this.datasinks = datasinks;
    this.monitortags = monitorTags;
    this.visorUuid = "";
    this.owner = userid;
  }

  public StateType getOwnTargetState() {
    return ownTargetState;
  }

  public void setOwnTargetState(StateType ownTargetState) {
    this.ownTargetState = ownTargetState;
  }

  public String getOwnTargetId() {
    return ownTargetId;
  }

  public TargetType getOwnTargetType() {
    return ownTargetType;
  }

  public String getVisorUuid() {
    return visorUuid;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public void setVisorUuid(String uuid) {
    this.visorUuid = uuid;
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

  public Map<String, String> getMonitortags() {
    return monitortags;
  }

  public SensorModel getSensor() {
    return sensor;
  }

  public void setSensor(SensorModel sensor) {
    this.sensor = sensor;
  }

  public void setTargets(List<TargetModel> targets) {
    this.targets = targets;
  }

  public void setMonitortags(Map<String, String> monitortags) {
    this.monitortags = monitortags;
  }

  public void addTarget(TargetModel targetModel) {
    if (targets == null) {
      targets = new ArrayList<TargetModel>();
    }
    targets.add(targetModel);
  }

  public void setDatasinks(List<DataSinkModel> datasinks) {
    this.datasinks = datasinks;
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

  public void updateTargets(List<TargetModel> targetsToUpdate) {
    //remove old targets
    for (TargetModel actualTarget : this.targets) {
      if (!targetsToUpdate.contains(actualTarget)) {
        this.targets.remove(actualTarget);
      }
    }
    //add new targets
    for (TargetModel updateTarget : targetsToUpdate) {
      if (!this.targets.contains(updateTarget)) {
        this.targets.add(updateTarget);
      }
    }
  }

}
