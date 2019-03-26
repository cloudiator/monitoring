package io.github.cloudiator.monitoring.messaging;


import com.google.inject.Inject;
import io.github.cloudiator.rest.converter.MonitorConverter;
import io.github.cloudiator.monitoring.domain.MonitorManagementService;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.monitoring.domain.MonitorOrchestrationService;
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
  private final MonitorManagementService monitorManagementService;
  private final MonitorConverter monitorConverter = new MonitorConverter();

  @Inject
  public CreateMonitorListener(MessageInterface messageInterface,
      MonitorManagementService monitorManagementService) {
    this.messageInterface = messageInterface;
    this.monitorManagementService = monitorManagementService;
  }


  @Override
  public void run() {
    messageInterface.subscribe(CreateMonitorRequest.class, CreateMonitorRequest.parser(),
        new MessageCallback<CreateMonitorRequest>() {
          @Override
          public void accept(String id, CreateMonitorRequest content) {
            try {

              Monitor contentMonitor = monitorConverter.applyBack(content.getNewmonitor());
              //LOGGER.debug("Got CreateMonitorCall: " + contentMonitor.toString());

              Monitor createdMonitor = monitorManagementService
                  .handleNewMonitor(content.getUserId(), contentMonitor);

              CreateMonitorResponse monitorResponse = CreateMonitorResponse.newBuilder()
                  .setMonitor(monitorConverter.apply(createdMonitor)).build();
              messageInterface.reply(id, monitorResponse);

            } catch (IllegalArgumentException ie) {
              LOGGER.error("IllegalState while creating Monitor. ", ie);
              messageInterface.reply(CreateMonitorResponse.class, id,
                  Error.newBuilder().setCode(400)
                      .setMessage("Illegal Argument by creating Monitor: " + ie.getMessage())
                      .build());
            } catch (AssertionError ar) {
              LOGGER.error("AssertionError occures: " + ar.getMessage());
              messageInterface.reply(CreateMonitorResponse.class, id,
                  Error.newBuilder().setCode(505).setMessage("AssertionError: " + ar.getMessage())
                      .build());
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
