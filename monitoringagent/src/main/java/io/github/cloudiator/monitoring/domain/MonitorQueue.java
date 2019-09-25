package io.github.cloudiator.monitoring.domain;

import com.google.inject.Inject;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import java.util.Queue;

public class MonitorQueue {

  private Queue<DomainMonitorModel> queue;
  private MonitorQueueConsumer monitorQueueConsumer;

  @Inject
  public MonitorQueue(Queue<DomainMonitorModel> queue, MonitorQueueConsumer monitorQueueConsumer) {
    this.queue = queue;
    this.monitorQueueConsumer = monitorQueueConsumer;
  }

  public MonitorQueueConsumer getMonitorQueueConsumer() {
    return monitorQueueConsumer;
  }

  public Queue<DomainMonitorModel> getQueue() {
    return queue;
  }

  public Enum getThreadState() {
    return null;
  }
}
