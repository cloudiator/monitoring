package io.github.cloudiator.monitoring.messaging;

import com.google.inject.Inject;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.monitoring.domain.MonitorManagementService;
import io.github.cloudiator.monitoring.models.TargetState;
import io.github.cloudiator.rest.converter.ProcessConverter;
import io.github.cloudiator.rest.model.CloudiatorProcess;
import io.github.cloudiator.rest.model.MonitoringTarget.TypeEnum;
import io.github.cloudiator.rest.model.SingleProcess;
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
  private final ProcessConverter PROCESS_CONVERTER = ProcessConverter.INSTANCE;

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
              System.out.println("Got processEvent: " + processEvent.toString());
              TargetState targetState;
              switch (processEvent.getTo()) {
                case PROCESS_STATE_PENDING:
                  targetState = TargetState.PENDING;
                  break;
                case PROCESS_STATE_RUNNING:
                  targetState = TargetState.RUNNING;
                  break;
                case PROCESS_STATE_DELETED:
                  targetState = TargetState.DELETED;
                  break;
                case PROCESS_STATE_FINISHED:
                  targetState = TargetState.FINISHED;
                  break;
                case PROCESS_STATE_ERROR:
                case UNRECOGNIZED:
                default:
                  targetState = TargetState.ERROR;
                  break;
              }
              monitorManagementService
                  .handleEvent(TypeEnum.PROCESS, processEvent.getProcess().getId(), targetState);
            } catch (Exception e) {
              LOGGER.error("Error while receiving NodeEvent. ", e);

            }
          }
        });
  }
}
