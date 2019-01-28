package io.github.cloudiator.monitoring.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.rest.converter.MonitorConverter;
import io.github.cloudiator.monitoring.domain.MonitorManagementService;
import io.github.cloudiator.monitoring.domain.MonitorOrchestrationService;
import io.github.cloudiator.rest.model.Monitor;
import java.util.NoSuchElementException;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Monitor.UpdateMonitorRequest;
import org.cloudiator.messages.Monitor.UpdateMonitorResponse;
import org.cloudiator.messages.entities.MonitorEntities;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateMonitorListener implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateMonitorListener.class);
  private final MessageInterface messageInterface;
  private final MonitorManagementService monitorManagementService;
  private final MonitorConverter monitorConverter = new MonitorConverter();

  @Inject
  public UpdateMonitorListener(MessageInterface messageInterface,
      MonitorManagementService monitorManagementService) {
    this.messageInterface = messageInterface;
    this.monitorManagementService = monitorManagementService;
  }

  @Override
  public void run() {
    messageInterface.subscribe(UpdateMonitorRequest.class, UpdateMonitorRequest.parser(),
        new MessageCallback<UpdateMonitorRequest>() {
          @Override
          public void accept(String id, UpdateMonitorRequest content) {
            try {
              Monitor dbmonitor = monitorManagementService
                  .getMonitor(content.getMonitor().getMetric());
              MonitorEntities.Monitor monitorResult;
              if (dbmonitor == null) {
                monitorResult = MonitorEntities.Monitor.newBuilder()
                    .setMetric(content.getMonitor().getMetric())
                    .clearTags()
                    .clearSensor()
                    .clearTarget()
                    .clearDatasink()
                    .build();
              } else {
                monitorResult = monitorConverter.apply(dbmonitor);
              }

              UpdateMonitorResponse.Builder responseBuilder = UpdateMonitorResponse.newBuilder()
                  .setMonitor(monitorResult);

              UpdateMonitorResponse result = responseBuilder.build();
              messageInterface.reply(id, result);
            } catch (IllegalArgumentException e) {
              LOGGER.error("Requested Monitor not present. ", e);
              messageInterface.reply(UpdateMonitorResponse.class, id,
                  Error.newBuilder().setCode(404).setMessage("Monitor not found").build());
            } catch (NoSuchElementException e) {
              LOGGER.error("No Monitor found. ", e);
              messageInterface.reply(UpdateMonitorResponse.class, id,
                  Error.newBuilder().setCode(400).setMessage("Monitor not found.").build());
            } catch (Exception e) {
              LOGGER.error("Error while searching for Monitors. ", e);
              messageInterface
                  .reply(UpdateMonitorResponse.class, id, Error.newBuilder().setCode(500)
                      .setMessage("Error while searching for Monitors " + e.getMessage()).build());
            }
          }
        });
  }
}
