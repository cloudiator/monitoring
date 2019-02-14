package io.github.cloudiator.monitoring.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.monitoring.domain.MonitorManagementService;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Monitor.MonitorQueryResponse;
import org.cloudiator.messages.Node.NodeDeleteResponseMessage;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NodeDeletedListener implements Runnable {


  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeDeletedListener.class);
  private final MessageInterface messageInterface;
  private final MonitorManagementService monitorManagementService;


  @Inject
  public NodeDeletedListener(MessageInterface messageInterface,
      MonitorManagementService monitorManagementService) {
    this.messageInterface = messageInterface;
    this.monitorManagementService = monitorManagementService;
  }

  @Override
  public void run() {
    messageInterface.subscribe(NodeDeleteResponseMessage.class, NodeDeleteResponseMessage.parser(),
        new MessageCallback<NodeDeleteResponseMessage>() {
          @Override
          public void accept(String id, NodeDeleteResponseMessage nodeDeleted) {
            try {

              messageInterface.reply(id, null);
            } catch (Exception e) {
              LOGGER.error("Error while searching for Monitors. ", e);
              messageInterface
                  .reply(NodeDeleteResponseMessage.class, id, Error.newBuilder().setCode(500)
                      .setMessage(
                          "MonitorError while handling NodeDeletedResponse" + e.getMessage())
                      .build());
            }
          }
        });
  }
}
