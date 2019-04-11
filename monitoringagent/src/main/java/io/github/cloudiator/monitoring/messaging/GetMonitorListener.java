package io.github.cloudiator.monitoring.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.rest.converter.MonitorConverter;
import io.github.cloudiator.monitoring.domain.MonitorManagementService;
import io.github.cloudiator.rest.converter.MonitorTargetConverter;
import java.util.NoSuchElementException;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Monitor.GetMonitorRequest;
import org.cloudiator.messages.Monitor.GetMonitorResponse;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetMonitorListener implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateMonitorListener.class);
  private final MessageInterface messageInterface;
  private final MonitorManagementService monitorManagementService;
  private final MonitorConverter monitorConverter = new MonitorConverter();
  private final MonitorTargetConverter targetConverter = MonitorTargetConverter.INSTANCE;

  @Inject
  public GetMonitorListener(MessageInterface messageInterface,
      MonitorManagementService monitorManagementService) {
    this.messageInterface = messageInterface;
    this.monitorManagementService = monitorManagementService;
  }

  @Override
  public void run() {
    messageInterface.subscribe(GetMonitorRequest.class, GetMonitorRequest.parser(),
        new MessageCallback<GetMonitorRequest>() {
          @Override
          public void accept(String id, GetMonitorRequest content) {
            try {
              DomainMonitorModel dbmonitor = null;

              dbmonitor = monitorManagementService
                  .getMonitor(content.getMetric(),
                      targetConverter.applyBack(content.getTarget()),content.getUserId());

              if (dbmonitor.getUuid() != null ) {
                dbmonitor.addTagItem("VisorUuid", dbmonitor.getUuid());
              }

              GetMonitorResponse.Builder responseBuilder = GetMonitorResponse.newBuilder()
                  .setMonitor(monitorConverter.apply(dbmonitor));

              GetMonitorResponse result = responseBuilder.build();
              messageInterface.reply(id, result);
            } catch (IllegalArgumentException e) {
              LOGGER.error("Requested Monitor not present. ", e);
              messageInterface.reply(GetMonitorResponse.class, id,
                  Error.newBuilder().setCode(404).setMessage("Monitor not found").build());
            } catch (NoSuchElementException e) {
              LOGGER.error("No Monitor found. ", e);
              messageInterface.reply(GetMonitorResponse.class, id,
                  Error.newBuilder().setCode(400).setMessage("Monitor not found.").build());
            } catch (Exception e) {
              LOGGER.error("Error while searching for Monitors. ", e);
              messageInterface
                  .reply(GetMonitorResponse.class, id, Error.newBuilder().setCode(500)
                      .setMessage("Error while searching for Monitors " + e.getMessage()).build());
            }
          }
        });
  }
}
