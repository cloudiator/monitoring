package io.github.cloudiator.monitoring.messaging;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.monitoring.converter.MonitorConverter;
import io.github.cloudiator.monitoring.domain.Monitor;
import io.github.cloudiator.monitoring.domain.MonitorOrchestrationService;
import org.cloudiator.messages.Monitor.MonitorQueryRequest;
import org.cloudiator.messages.Monitor.MonitorQueryResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.cloudiator.messages.General.Error;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorQueryListener implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorQueryListener.class);
  private final MessageInterface messageInterface;
  private final MonitorOrchestrationService monitorOrchestrationService;
  private final MonitorConverter monitorConverter = MonitorConverter.MONITOR_CONVERTER;

  @Inject
  public MonitorQueryListener(MessageInterface messageInterface,
      MonitorOrchestrationService monitorOrchestrationService) {
    this.messageInterface = messageInterface;
    this.monitorOrchestrationService = monitorOrchestrationService;
  }

  @Transactional
  private Iterable<Monitor> getAllMonitors() {
    return monitorOrchestrationService.getAllMonitors();
  }


  @Override
  public void run() {
    messageInterface.subscribe(MonitorQueryRequest.class, MonitorQueryRequest.parser(),
        new MessageCallback<MonitorQueryRequest>() {
          @Override
          public void accept(String id, MonitorQueryRequest content) {
            try {
              System.out.println("Got message: ");

              MonitorQueryResponse.Builder responseBuilder = MonitorQueryResponse.newBuilder();
              for (Monitor monitor : getAllMonitors()) {
                responseBuilder.addMonitor(monitorConverter.apply(monitor));
              }
              messageInterface.reply(id, responseBuilder.build());
            } catch (Exception e) {
              LOGGER.error("Error while searching for Monitors. ", e);
              messageInterface.reply(MonitorQueryResponse.class, id, Error.newBuilder().setCode(500)
                  .setMessage("Error while searching for Monitors " + e.getMessage()).build());
            }

          }
        });

  }
}
