package io.github.cloudiator.monitoring.domain;

import com.google.inject.Inject;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorQueueConsumer implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorQueueConsumer.class);
  private final MonitorQueueController monitorQueueController;
  private final String nodeId;
  private final MonitorHandler monitorHandler;

  private Queue<DomainMonitorModel> queue;

  @Inject
  public MonitorQueueConsumer(String nodeId, Queue<DomainMonitorModel> queue,
      MonitorQueueController monitorQueueController, MonitorHandler monitorHandler) {
    this.nodeId = nodeId;
    this.queue = queue;
    this.monitorQueueController = monitorQueueController;
    this.monitorHandler = monitorHandler;
  }

  @Override
  public void run() {

    while (!queue.isEmpty()) {
      DomainMonitorModel actualMonitor = queue.poll();
      LOGGER.debug("handling Monitor: " + actualMonitor.getMetric());
      LOGGER.debug(queue.size() + " remaining Monitors in Queue");
      monitorHandler.handleNodeMonitor(actualMonitor.getOwner(), actualMonitor);

    }
    LOGGER.debug("Removing empty NodeQueue: " + nodeId);
    monitorQueueController.removeQueue(nodeId);
  }
}
