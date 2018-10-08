package io.github.cloudiator.persistance;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;

@Entity
public class IntervalModel extends Model {

  @Column
  private Long period;

  @Enumerated(EnumType.STRING)
  @Column
  private Unit unit;

  @OneToMany(cascade = CascadeType.ALL, mappedBy = "interval")
  private Set<PullSensorModel> sensorModelSet;

  public Long getPeriod() {
    return period;
  }

  public void setPeriod(Long period) {
    this.period = period;
  }

  public Unit getUnit() {
    return unit;
  }

  public void setUnit(Unit unit) {
    this.unit = unit;
  }




}
