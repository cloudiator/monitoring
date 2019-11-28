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
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorQueueController {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorQueueController.class);
  private final MonitorHandler monitorHandler;

  private static final Map<String, Queue<DomainMonitorModel>> queueMap = new ConcurrentHashMap<>();
  private static final Map<String, MonitorQueue> monitorQueueMap = new ConcurrentHashMap<>();
  private static final ThreadPoolExecutor queueExecutor = (ThreadPoolExecutor) Executors
      .newFixedThreadPool(10);

  static {
    MoreExecutors.addDelayedShutdownHook(queueExecutor, 100, TimeUnit.MILLISECONDS);
    LOGGER.info("MonitorQueueController initialized");
  }

  @Inject
  public MonitorQueueController(MonitorHandler monitorHandler) {
    this.monitorHandler = monitorHandler;
  }

  private Queue<DomainMonitorModel> createNewQueue(String nodeId, DomainMonitorModel request) {
    LOGGER.debug("add new Queue ");
    Queue<DomainMonitorModel> result = new LinkedList<>();
    result.add(request);
    queueMap.put(nodeId, result);
    LOGGER.debug("new QueueMapSize: " + queueMap.size());
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
    queueMap.remove(nodeId);
  }

  private synchronized void handleQueueMap(QueueAction action,
      DomainMonitorModel domainMonitorModel) {
  }

  public int getQueueMapSize() {

    return queueMap.size();
  }

  public synchronized boolean handleMonitorRequest(String nodeId, DomainMonitorModel domainMonitorModel) {

    LOGGER.debug("QueueController handling Monitor "+domainMonitorModel.getMetric());
    Queue<DomainMonitorModel> usedQueue;
    if (existingQueue(nodeId)) {
      LOGGER.debug("found existing Queue, adding Monitor");
      usedQueue = getQueue(nodeId);
      usedQueue.add(domainMonitorModel);
      LOGGER.debug("QueueSize now: " + usedQueue.size());
    } else {
      usedQueue = createNewQueue(nodeId, domainMonitorModel);
      LOGGER.debug("created new Queue with size: " + queueMap.size() + " - starting new QueueConsumer");
      queueExecutor.submit(new MonitorQueueConsumer(nodeId, usedQueue, this, monitorHandler));
    }

    return true;
  }

  private enum QueueAction {
    ADD,
    UPDATE,
    REMOVE;
  }


}
