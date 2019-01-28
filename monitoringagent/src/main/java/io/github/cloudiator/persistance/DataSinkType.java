package io.github.cloudiator.persistance;

public enum DataSinkType {
  KAIROS_DB,
  INFLUX,
  CLI,
  JMS;


  private DataSinkType() {
  }
}
