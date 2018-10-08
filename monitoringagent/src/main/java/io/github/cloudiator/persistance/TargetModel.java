package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;

@Entity
public class TargetModel extends Model {

  public enum TargetEnum {CLOUD, NODE, JOB, TASK, PROCESS;}

  @Column
  @Enumerated(EnumType.STRING)
  private TargetEnum targetType;

  @Column
  private String identifier;

  @ManyToMany(mappedBy = "targets")
  private Set<MonitorModel> monitors;

  protected TargetModel() {

  }

  public TargetModel(TargetEnum targetType, String identifier, Set<MonitorModel> monitors) {
    this.targetType = targetType;
    this.monitors = monitors;
    this.identifier = identifier;
  }


  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public TargetEnum getTargetType() {
    return targetType;
  }

  public void setTargetType(TargetEnum targetType) {
    this.targetType = targetType;
  }

  public Set<MonitorModel> getMonitors() {
    return monitors;
  }

  public void addMonitor(MonitorModel monitor) {
    checkNotNull(monitor, "monitor ist null.");
    if (monitor == null) {
      monitors = new HashSet<MonitorModel>();
    }
    if (!monitors.contains(monitor)) {
      monitors.add(monitor);
    }
  }
}
