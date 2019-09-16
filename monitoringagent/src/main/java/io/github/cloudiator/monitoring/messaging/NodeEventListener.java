package io.github.cloudiator.monitoring.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.monitoring.domain.MonitorManagementService;
import io.github.cloudiator.monitoring.models.TargetState;
import io.github.cloudiator.rest.converter.MonitorConverter;
import io.github.cloudiator.rest.model.MonitoringTarget.TypeEnum;
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
              TargetState targetState;
              LOGGER.debug("Got NodeEvent: " + nodeEvent.toString());
              switch (nodeEvent.getTo()) {
                case NODE_STATE_PENDING:
                  targetState = TargetState.PENDING;
                  break;
                case NODE_STATE_RUNNING:
                  targetState = TargetState.RUNNING;
                  break;
                case NODE_STATE_DELETED:
                  targetState = TargetState.DELETED;
                  break;
                case NODE_STATE_ERROR:
                case UNRECOGNIZED:
                default:
                  targetState = TargetState.ERROR;
                  break;
              }
              monitorManagementService
                  .handleEvent(TypeEnum.NODE, nodeEvent.getNode().getId(), targetState);

            } catch (Exception e) {
              LOGGER.error("Error while receiving NodeEvent. ", e);

            }
          }
        });
  }
}
