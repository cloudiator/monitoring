package io.github.cloudiator.monitoring.domain;

import com.google.inject.Inject;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import java.util.Queue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorQueueConsumer extends Thread  /*implements Runnable*/ {

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
    try {
      while (!queue.isEmpty()) {
        DomainMonitorModel actualMonitor = queue.poll();
        LOGGER.debug("handling Monitor: " + actualMonitor.getMetric());
        LOGGER.debug(queue.size() + " remaining Monitors in Queue");
        monitorHandler.handleNodeMonitor(actualMonitor.getOwner(), actualMonitor);
      }
      LOGGER.debug("Removing empty NodeQueue: " + nodeId);
      monitorQueueController.removeQueue(nodeId);

    } catch (IllegalStateException e) {
      LOGGER.debug("IllegalStateException: " + e);
      LOGGER.debug(
          "remaining QueueSize: " + queue.size() + "cause of the error the queue will be deleted");
      queue.clear();
      monitorQueueController.removeQueue(nodeId);
    } catch (AssertionError a) {
      LOGGER.debug("AssertionError: " + a);
      LOGGER.debug("Because of Problems by finding the node " + nodeId
          + "\n removing related MonitorQueue. ");
      LOGGER.debug("remaining QueueSize: " + queue.size());
      queue.clear();
      monitorQueueController.removeQueue(nodeId);
    } catch (Exception e) {
      LOGGER.debug("Exception happend: " + e);
      LOGGER.debug(
          "remaining QueueSize: " + queue.size() + "cause of the error the queue will be deleted");
      queue.clear();
      monitorQueueController.removeQueue(nodeId);
    }
  }
}
