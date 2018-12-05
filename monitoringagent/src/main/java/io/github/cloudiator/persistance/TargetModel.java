package io.github.cloudiator.persistance;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;

@Entity
public class TargetModel extends Model {

  @Column
  @Enumerated(EnumType.STRING)
  private TargetType targetType;

  @Column
  private String identifier;


  protected TargetModel() {
  }

  public TargetModel(TargetType targetType, String identifier) {
    this.targetType = targetType;
    this.identifier = identifier;
  }

  public TargetModel targetType(TargetType targetType) {
    this.targetType = targetType;
    return this;
  }

  public TargetModel identifier(String identifier) {
    this.identifier = identifier;
    return this;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public TargetType getTargetType() {
    return targetType;
  }

  public void setTargetType(TargetType targetType) {
    this.targetType = targetType;
  }

}
