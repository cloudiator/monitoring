package io.github.cloudiator.monitoring.domain;

import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class MonitorQueueController {

  private static final Map<String, Queue<DomainMonitorModel>> nodeControl = new ConcurrentHashMap<>();

  private Queue<DomainMonitorModel> createNewQueue(String nodeId, DomainMonitorModel request) {
    Queue<DomainMonitorModel> result = new LinkedList<>();
    result.add(request);
    nodeControl.put(nodeId, result);
    return result;
  }

  private boolean existingQueue(String nodeId) {
    if (nodeControl.containsKey(nodeId)) {
      return true;
    }
    return false;
  }

  private Queue<DomainMonitorModel> getQueue(String nodeId) {
    Queue<DomainMonitorModel> result = nodeControl.get(nodeId);
    return result;
  }

  public void checkQueue() {
  }


  public boolean handleMonitorRequest(String nodieId, DomainMonitorModel domainMonitorModel) {
    Queue<DomainMonitorModel> usedQueue;
    if (existingQueue(nodieId)){
      usedQueue = getQueue(nodieId);
      usedQueue.add(domainMonitorModel);
    }else{
      usedQueue = createNewQueue(nodieId, domainMonitorModel);
    }

    return false;
  }

}
