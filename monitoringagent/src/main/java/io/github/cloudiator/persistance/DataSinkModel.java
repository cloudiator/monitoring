package io.github.cloudiator.persistance;



import java.util.HashMap;
import java.util.Map;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;



@Entity
public class DataSinkModel extends Model {

  @Enumerated(EnumType.STRING)
  @Column
  private DataSinkType sinkType;


  @ElementCollection
  private Map<String, String> configuration;

  protected DataSinkModel() {
  }

  public DataSinkModel sinkType(String sinktype) {
    this.sinkType = DataSinkType.valueOf(sinktype);
    return this;
  }

  public DataSinkModel configuration(Map configuration) {
    Map config = new HashMap();
    config.putAll(configuration);
    this.configuration = config;
    return this;
  }


  public DataSinkType getSinkType() {
    return sinkType;
  }

  public Map<String, String> getConfiguration() {
    return configuration;
  }

  public void setConfiguration(Map configuration) {
    if (this.configuration == null) {
      this.configuration = new HashMap();
    }
    this.configuration.putAll(configuration);
  }

  public void setSinkType(DataSinkType sinktype) {
    this.sinkType = sinktype;
  }

}
