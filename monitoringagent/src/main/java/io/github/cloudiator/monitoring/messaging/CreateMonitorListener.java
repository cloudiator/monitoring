package io.github.cloudiator.monitoring.messaging;


import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.monitoring.converter.MonitorConverter;
import io.github.cloudiator.monitoring.domain.Monitor;
import io.github.cloudiator.monitoring.domain.MonitorOrchestrationService;
import java.util.Optional;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Monitor.CreateMonitorRequest;
import org.cloudiator.messages.Monitor.CreateMonitorResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateMonitorListener implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateMonitorListener.class);
  private final MessageInterface messageInterface;
  private final MonitorOrchestrationService monitorOrchestrationService;
  private final MonitorConverter monitorConverter = MonitorConverter.MONITOR_CONVERTER;

  @Inject
  public CreateMonitorListener(MessageInterface messageInterface,
      MonitorOrchestrationService monitorOrchestrationService) {
    this.messageInterface = messageInterface;
    this.monitorOrchestrationService = monitorOrchestrationService;
  }

  @Transactional
  private Optional<Monitor> checkAndCreate(Monitor monitor) {
    Optional<Monitor> dbMonitor = monitorOrchestrationService
        .getMonitor(monitor.getMetric());
    if (dbMonitor.isPresent()) {
      return Optional.empty();
    } else {
      dbMonitor = Optional.of(monitorOrchestrationService.createMonitor(monitor));
      return dbMonitor;
    }
  }


  @Override
  public void run() {
    messageInterface.subscribe(CreateMonitorRequest.class, CreateMonitorRequest.parser(),
        new MessageCallback<CreateMonitorRequest>() {
          @Override
          public void accept(String id, CreateMonitorRequest content) {
            try {
              Monitor newMonitor = monitorConverter.applyBack(content.getNewmonitor());
              Optional<Monitor> requestedMonitor = checkAndCreate(newMonitor);
              if (!requestedMonitor.isPresent()) {
                messageInterface.reply(CreateMonitorResponse.class, id,
                    Error.newBuilder().setCode(400)
                        .setMessage("Monitor or Monitormetric already exist.").build());
                return;
              } else {
                CreateMonitorResponse monitorResponse = CreateMonitorResponse.newBuilder()
                    .setMonitor(monitorConverter.apply(requestedMonitor.get())).build();
                messageInterface.reply(id, monitorResponse);
              }
            } catch (Exception e) {
              LOGGER.error("Error while creating Monitor. ", e);
              messageInterface.reply(CreateMonitorResponse.class, id,
                  Error.newBuilder().setCode(500)
                      .setMessage("Error while creating Monitor: " + e.getMessage()).build());
            }
          }
        });

  }
}
