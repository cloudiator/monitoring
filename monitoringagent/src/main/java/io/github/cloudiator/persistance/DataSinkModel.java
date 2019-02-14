package io.github.cloudiator.persistance;


import io.github.cloudiator.rest.model.DataSink;
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
  private DataSink.TypeEnum sinkType;


  @ElementCollection
  private Map<String, String> configuration;

  protected DataSinkModel() {
  }

  public DataSinkModel sinkType(String sinktype) {
    this.sinkType = DataSink.TypeEnum.valueOf(sinktype);
    return this;
  }

  public DataSinkModel configuration(Map configuration) {
    Map config = new HashMap();
    config.putAll(configuration);
    this.configuration = config;
    return this;
  }


  public DataSink.TypeEnum getSinkType() {
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

  public void setSinkType(DataSink.TypeEnum sinktype) {
    this.sinkType = sinktype;
  }

}
