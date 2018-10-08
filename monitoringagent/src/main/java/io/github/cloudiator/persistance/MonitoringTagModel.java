package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;

@Entity
public class MonitoringTagModel extends Model {

  @Column(nullable = false, name = "tagKey")
  private String key;

  @Column
  @Lob
  private String value;

  @ManyToMany(mappedBy = "monitortags")
  private Set<MonitorModel> monitors;

  protected MonitoringTagModel() {
  }

  public MonitoringTagModel(String key, String value, Set<MonitorModel> monitors) {
    checkNotNull(key);
    checkNotNull(value);
    this.key = key;
    this.value = value;
    this.monitors = monitors;
  }


  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public Set<MonitorModel> getMonitors() {
    return monitors;
  }

  public void addMonitor(MonitorModel monitorModel) {
    if (monitors == null) {
      monitors = new HashSet<MonitorModel>();
    }
    monitors.add(monitorModel);
  }


}
