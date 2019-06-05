package io.github.cloudiator.monitoring.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.rest.converter.MonitorConverter;
import io.github.cloudiator.monitoring.domain.MonitorManagementService;
import io.github.cloudiator.rest.converter.MonitorTargetConverter;
import org.cloudiator.messages.Monitor.DeleteMonitorRequest;
import org.cloudiator.messages.Monitor.DeleteMonitorResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cloudiator.messages.General.Error;

public class DeleteMonitorListener implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteMonitorListener.class);
  private final MessageInterface messageInterface;
  private final MonitorManagementService monitorManagementService;
  private final MonitorTargetConverter targetConverter = MonitorTargetConverter.INSTANCE;

  @Inject
  public DeleteMonitorListener(MessageInterface messageInterface,
      MonitorManagementService monitorManagementService) {
    this.messageInterface = messageInterface;
    this.monitorManagementService = monitorManagementService;
  }

  @Override
  public void run() {
    messageInterface.subscribe(DeleteMonitorRequest.class, DeleteMonitorRequest.parser(),
        new MessageCallback<DeleteMonitorRequest>() {
          @Override
          public void accept(String id, DeleteMonitorRequest content) {
            try {

              monitorManagementService.deleteMonitor(content.getUserId(), content.getMetric(),
                  targetConverter.applyBack(content.getTarget()));

              DeleteMonitorResponse.Builder responseBuilder = DeleteMonitorResponse.newBuilder();

              DeleteMonitorResponse result = responseBuilder.build();
              messageInterface.reply(id, result);
            } catch (Exception e) {
              LOGGER.error("Error while deleting Monitor. ", e);
              messageInterface
                  .reply(DeleteMonitorResponse.class, id, Error.newBuilder().setCode(500)
                      .setMessage("Error while deleting Monitor " + e.getMessage()).build());
            }

          }
        });
  }
}
