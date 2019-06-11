package io.github.cloudiator.monitoring.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.monitoring.domain.MonitorManagementService;
import io.github.cloudiator.rest.converter.ProcessConverter;
import io.github.cloudiator.rest.model.CloudiatorProcess;
import org.cloudiator.messages.Node.NodeEvent;
import org.cloudiator.messages.Process;
import org.cloudiator.messages.Process.ProcessEvent;
import org.cloudiator.messaging.MessageCallback;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ProcessEventListener implements Runnable {


  private static final Logger LOGGER = LoggerFactory
      .getLogger(ProcessEventListener.class);
  private final MessageInterface messageInterface;
  private final MonitorManagementService monitorManagementService;
  private final ProcessConverter processConverter = ProcessConverter.INSTANCE;

  @Inject
  public ProcessEventListener(MessageInterface messageInterface,
      MonitorManagementService monitorManagementService) {
    this.messageInterface = messageInterface;
    this.monitorManagementService = monitorManagementService;
  }

  @Override
  public void run() {
    messageInterface.subscribe(ProcessEvent.class, ProcessEvent.parser(),
        new MessageCallback<ProcessEvent>() {
          @Override
          public void accept(String id, ProcessEvent processEvent) {
            try {
              System.out.println("Got processEvent: "+processEvent.toString());
              switch(processEvent.getTo()){
                case PROCESS_STATE_PENDING:

                  break;
                case PROCESS_STATE_RUNNING:

                  break;
                case PROCESS_STATE_DELETED:
                  System.out.println("Got deleted Event ");
                  CloudiatorProcess deletedProcess = processConverter.applyBack(processEvent.getProcess());
                  monitorManagementService.handledeletedProcess(deletedProcess, processEvent.getUserId());

                  //Node node = nodeMessageConverter.applyBack(nodeEvent.getNode());
                  //monitorManagementService.handeldeletedNode(node, nodeEvent.getNode().getUserId());

                  break;
                case PROCESS_STATE_ERROR:

                  break;
                case PROCESS_STATE_FINISHED:
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
