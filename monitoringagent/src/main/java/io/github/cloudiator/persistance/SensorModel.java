package io.github.cloudiator.persistance;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToOne;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
abstract class SensorModel extends Model {

  protected SensorModel() {
  }

  @OneToOne
  private MonitorModel monitor;


  public MonitorModel getMonitor() {
    return monitor;
  }

  public void setMonitor(MonitorModel monitor) {
    this.monitor = monitor;
  }


}
