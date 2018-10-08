package io.github.cloudiator.persistance;


import static com.google.common.base.Preconditions.checkNotNull;


import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
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

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "dataSink")
  private Set<DataSinkConfigurationModel> dataSinkConfiguration;


  protected DataSinkModel() {
  }

  public DataSinkModel(MonitorModel monitor, DataSinkType type,
      Set<DataSinkConfigurationModel> dataSinkConfigurationModels) {
    checkNotNull(monitor, "MonitorModel is null");
    checkNotNull(type, "DataSinkType is null");
    checkNotNull(dataSinkConfigurationModels, "DataSinkConfigurationModels is null");
    this.type = type;
    this.dataSinkConfiguration = dataSinkConfigurationModels;
    this.monitor = monitor;
  }


  public DataSinkType getType() {
    return type;
  }

  public Set<DataSinkConfigurationModel> getDataSinkConfiguration() {
    return dataSinkConfiguration;
  }

  public MonitorModel getMonitor() {
    return monitor;
  }

  public void setType(DataSinkType type) {
    this.type = type;
  }


  public void addConfiguration(DataSinkConfigurationModel dataSinkConfigurationModel) {
    checkNotNull(dataSinkConfigurationModel, "DataSinkConfigurationModel is null");
    if (dataSinkConfiguration == null) {
      dataSinkConfiguration = new HashSet<>();
    }
    dataSinkConfiguration.add(dataSinkConfigurationModel);
  }

  public void addConfiguration(String key, String value) {
    DataSinkConfigurationModel configurationModel = new DataSinkConfigurationModel(this, key,
        value);
    addConfiguration(configurationModel);
  }
}
