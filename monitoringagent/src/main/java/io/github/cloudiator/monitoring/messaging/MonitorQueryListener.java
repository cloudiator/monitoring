package io.github.cloudiator.monitoring.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.rest.converter.MonitorConverter;
import io.github.cloudiator.monitoring.domain.MonitorManagementService;
import io.github.cloudiator.rest.model.Monitor;
import java.util.Collection;
import java.util.List;
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
  private final MonitorManagementService monitorManagementService;
  private final MonitorConverter monitorConverter = new MonitorConverter();

  @Inject
  public MonitorQueryListener(MessageInterface messageInterface,
      MonitorManagementService monitorManagementService) {
    this.messageInterface = messageInterface;
    this.monitorManagementService = monitorManagementService;
  }

  @Override
  public void run() {
    messageInterface.subscribe(MonitorQueryRequest.class, MonitorQueryRequest.parser(),
        new MessageCallback<MonitorQueryRequest>() {
          @Override
          public void accept(String id, MonitorQueryRequest content) {
            try {

              List<DomainMonitorModel> dbmonitors = monitorManagementService
                  .getAllYourMonitors(content.getUserId());
              MonitorQueryResponse.Builder responseBuilder = MonitorQueryResponse.newBuilder();
              for (DomainMonitorModel monitor : dbmonitors) {
                //Writing DomainInformation to MonitorTags
                monitor.addTagItem("ownTarget: ",
                    " " + monitor.getOwnTargetType().toString() + "--" + monitor.getOwnTargetId());
                monitor.addTagItem("ownTargetState:",
                    " " + monitor.getOwnTargetState().toString());
                monitor.addTagItem("VisorUuid:", " " + monitor.getUuid());

                responseBuilder.addMonitor(monitorConverter.apply(monitor));
              }
              MonitorQueryResponse result = responseBuilder.build();

              messageInterface.reply(id, result);
            } catch (Exception e) {
              LOGGER.error("Error while searching for Monitors. ", e);
              messageInterface.reply(MonitorQueryResponse.class, id, Error.newBuilder().setCode(500)
                  .setMessage("Error while searching for Monitors: " + e.getMessage()).build());
            }

          }
        });
  }
}
