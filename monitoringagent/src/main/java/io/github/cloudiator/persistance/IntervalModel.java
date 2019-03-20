package io.github.cloudiator.persistance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Entity
public class IntervalModel extends BaseModel {

  @Column
  private Long period;

  @Enumerated(EnumType.STRING)
  @Column
  private Unit unit;

  protected IntervalModel() {

  }

  public IntervalModel(Unit unit, Long period) {
    this.unit = unit;
    this.period = period;
  }

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
