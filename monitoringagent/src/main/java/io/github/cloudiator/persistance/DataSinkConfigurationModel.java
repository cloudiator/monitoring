package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Entity
public class DataSinkConfigurationModel extends Model {

  @Column(name = "confKey")
  private String key;

  @Column
  private String value;

  @ManyToOne(optional = false,targetEntity = DataSinkModel.class)
  private DataSinkModel dataSink;

  protected DataSinkConfigurationModel() {
  }

  public DataSinkConfigurationModel(DataSinkModel dataSinkModel, String key, String value) {
    checkNotNull(dataSinkModel);
    checkNotNull(key);
    checkNotNull(value);
    checkArgument(!key.isEmpty(), "key is empty");
    checkArgument(!value.isEmpty(), "value is empty");
    this.dataSink = dataSinkModel;
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public DataSinkModel getDataSink() {
    return dataSink;
  }
}
