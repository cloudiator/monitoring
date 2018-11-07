package io.github.cloudiator.persistance;


import static com.google.common.base.Preconditions.checkNotNull;


import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class DataSinkModel extends Model {


  @Enumerated(EnumType.STRING)
  @Column
  private DataSinkType type;

  @ManyToOne
  private MonitorModel monitor;

  @ElementCollection
  private Map<String, String> configuration;

  protected DataSinkModel() {
  }

  public DataSinkModel(MonitorModel monitor, DataSinkType type, Map configuration) {
    checkNotNull(monitor, "MonitorModel is null");
    checkNotNull(type, "DataSinkType is null");
    checkNotNull(configuration, "DataSinkConfiguration is null");
    this.type = type;
    this.configuration = configuration;
    this.monitor = monitor;
  }

  public DataSinkType getType() {
    return type;
  }

  public Map<String, String> getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Map<String, String> configuration) {
    this.configuration = configuration;
  }

  public MonitorModel getMonitor() {
    return monitor;
  }

  public void setType(DataSinkType type) {
    this.type = type;
  }


}
