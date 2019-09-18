package io.github.cloudiator.persistance;

public enum StateType {
  PENDING,
  RUNNING,
  DELETED,
  ERROR,
  FINISHED;

  private StateType() {
  }
}
