package io.github.cloudiator.persistance;

public enum StateType {
  PENDING,
  RUNNING,
  DELETED,
  ERROR;

  private StateType() {
  }
}
