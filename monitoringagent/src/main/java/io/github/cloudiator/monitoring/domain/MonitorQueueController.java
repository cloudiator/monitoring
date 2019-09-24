package io.github.cloudiator.monitoring.domain;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorQueueController {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorQueueController.class);
  private final MonitorHandler monitorHandler;

  private static final Map<String, Queue<DomainMonitorModel>> queueMap = new ConcurrentHashMap<>();
  private static final ExecutorService queueExecutor = Executors.newFixedThreadPool(10);

  static {
    MoreExecutors.addDelayedShutdownHook(queueExecutor, 100, TimeUnit.MILLISECONDS);
    LOGGER.info("MonitorQueueController initialized");
  }

  @Inject
  public MonitorQueueController(MonitorHandler monitorHandler) {
    this.monitorHandler = monitorHandler;
  }

  private Queue<DomainMonitorModel> createNewQueue(String nodeId, DomainMonitorModel request) {
    LOGGER.debug("add new Queue");
    Queue<DomainMonitorModel> result = new LinkedList<>();
    result.add(request);
    queueMap.put(nodeId, result);
    LOGGER.debug("QueueMapSize: " + queueMap.size());
    return result;
  }

  private boolean existingQueue(String nodeId) {
    if (queueMap.containsKey(nodeId)) {
      return true;
    }
    return false;
  }

  private Queue<DomainMonitorModel> getQueue(String nodeId) {
    Queue<DomainMonitorModel> result = queueMap.get(nodeId);
    return result;
  }

  public void removeQueue(String nodeId) {
    LOGGER.debug("removing MapEntry");
    queueMap.remove(nodeId);
    LOGGER.debug("remaining Map: " + queueMap.size());
  }

  private synchronized void handleQueueMap(QueueAction action,
      DomainMonitorModel domainMonitorModel) {
  }


  public boolean handleMonitorRequest(String nodeId, DomainMonitorModel domainMonitorModel) {
    LOGGER.debug("handling Monitor");
    Queue<DomainMonitorModel> usedQueue;
    if (existingQueue(nodeId)) {
      usedQueue = getQueue(nodeId);
      usedQueue.add(domainMonitorModel);
    } else {
      usedQueue = createNewQueue(nodeId, domainMonitorModel);
      LOGGER.debug("QueueMapEntries: " + queueMap.size() + " - running newConsumer");
      queueExecutor.execute(new MonitorQueueConsumer(nodeId, usedQueue, this, monitorHandler));
    }
    return true;
  }

  private enum QueueAction {
    ADD,
    UPDATE,
    REMOVE;
  }

}
