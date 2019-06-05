package io.github.cloudiator.monitoring.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.monitoring.domain.MonitorManagementService;
import io.github.cloudiator.rest.converter.MonitorConverter;
import org.cloudiator.messages.General.Error;
import org.cloudiator.messages.Monitor.MonitorQueryRequest;
import org.cloudiator.messages.Monitor.MonitorQueryResponse;
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
  private final NodeToNodeMessageConverter nodeMessageConverter = NodeToNodeMessageConverter.INSTANCE;


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
              System.out.println("Got Event: "+nodeEvent.toString());
              switch(nodeEvent.getTo()){
                case NODE_STATE_PENDING:

                  break;
                case NODE_STATE_RUNNING:

                  break;
                case NODE_STATE_DELETED:
                  System.out.println("Got deleted Event ");
                  Node node = nodeMessageConverter.applyBack(nodeEvent.getNode());
                  monitorManagementService.handeldeletedNode(node, nodeEvent.getNode().getUserId());

                  break;
                case NODE_STATE_ERROR:

                  break;
                case UNRECOGNIZED:
                default:
                  break;
              }

            } catch (Exception e) {
              LOGGER.error("Error while receiving NodeEvent. ", e);

            }
          }
        });
  }
}
