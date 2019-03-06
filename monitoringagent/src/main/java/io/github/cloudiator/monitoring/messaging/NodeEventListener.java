package io.github.cloudiator.monitoring.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.monitoring.domain.MonitorManagementService;
import io.github.cloudiator.rest.converter.MonitorConverter;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.General.Response;
import org.cloudiator.messages.Monitor.MonitorQueryRequest;
import org.cloudiator.messages.Monitor.MonitorQueryResponse;
import org.cloudiator.messages.Node.NodeDeleteResponseMessage;
import org.cloudiator.messages.Node.NodeEvent;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NodeEventListener implements Runnable {


  private static final Logger LOGGER = LoggerFactory
      .getLogger(NodeEventListener.class);
  private final MessageInterface messageInterface;
  private final MonitorManagementService monitorManagementService;


  @Inject
  public NodeEventListener(MessageInterface messageInterface,
      MonitorManagementService monitorManagementService) {
    this.messageInterface = messageInterface;
    this.monitorManagementService = monitorManagementService;
  }

  @Override
  public void run() {
    messageInterface.subscribe(NodeEvent.class, NodeEvent.parser(),
        new MessageCallback<NodeEvent>() {
          @Override
          public void accept(String id, NodeEvent nodeEvent) {
            try {
              LOGGER.debug("Got one NodeEvent: " + nodeEvent.toString());
              System.out.println("GOT ONE EVENT: " + nodeEvent.toString());

              Response response = Response.newBuilder().setCorrelation("nice try").build();

              messageInterface.reply(id, response);
            } catch (Exception e) {
              LOGGER.error("Error getting NodeEvent. ", e);
              messageInterface.reply(NodeEvent.class, id, Error.newBuilder().setCode(500)
                  .setMessage("Error while getting NodeEvent " + e.getMessage()).build());
            }
          }
        });
  }
}
