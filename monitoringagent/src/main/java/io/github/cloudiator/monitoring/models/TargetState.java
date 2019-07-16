package io.github.cloudiator.monitoring.models;

public enum TargetState {
  PENDING,
  RUNNING,
  DELETED,
  ERROR;

  private TargetState() {
  }
}
