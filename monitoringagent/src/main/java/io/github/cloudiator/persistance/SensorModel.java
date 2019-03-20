package io.github.cloudiator.persistance;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
abstract class SensorModel extends BaseModel {

  protected SensorModel() {
  }


}
