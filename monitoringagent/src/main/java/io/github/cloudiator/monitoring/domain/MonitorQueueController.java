package io.github.cloudiator.monitoring.domain;

import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class MonitorQueueController {

  private static final Map<String, Queue<DomainMonitorModel>> nodeControl = new ConcurrentHashMap<>();

  public void checkQueue() {

  }

}
