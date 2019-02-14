package io.github.cloudiator.persistance;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Deprecated
@Entity
public class MTagModel extends Model {

  @Column(nullable = false)
  private String mkey;

  @Column
  private String mvalue;


  protected MTagModel() {
  }

  public MTagModel(String key, String value) {
    this.mkey = key;
    this.mvalue = value;
  }


  public String getKey() {
    return mkey;
  }

  public String getValue() {
    return mvalue;
  }

  public void setValue(String value) {
    this.mvalue = value;
  }


}
