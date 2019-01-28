package io.github.cloudiator.monitoring.domain;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.rest.converter.ProcessConverter;
import io.github.cloudiator.rest.model.CloudiatorProcess;
import io.github.cloudiator.rest.model.ClusterProcess;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.rest.model.SingleProcess;
import io.github.cloudiator.domain.Node;
import java.util.List;
import java.util.Optional;
import org.cloudiator.messages.Process.ProcessQueryRequest;
import org.cloudiator.messages.Process.ProcessQueryResponse;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MonitorManagementService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorManagementService.class);

  private final MonitorHandler monitorHandler;
  private final MonitorOrchestrationService monitorOrchestrationService;
  private final ProcessService processService;
  private final NodeToNodeMessageConverter nodeMessageConverter = NodeToNodeMessageConverter.INSTANCE;

  @Inject
  public MonitorManagementService(MonitorHandler monitorHandler,
      BasicMonitorOrchestrationService monitorOrchestrationService, ProcessService processService) {
    this.monitorHandler = monitorHandler;
    this.monitorOrchestrationService = monitorOrchestrationService;
    this.processService = processService;
  }

  @Transactional
  public Monitor checkAndCreate(Monitor monitor) {
    Optional<Monitor> dbMonitor = monitorOrchestrationService
        .getMonitor(monitor.getMetric());
    if (dbMonitor.isPresent()) {
      return null;
    } else {
      dbMonitor = Optional.of(monitorOrchestrationService.createMonitor(monitor));
      return dbMonitor.get();
    }
  }

  @Transactional
  public Monitor addTarget2Monitor(Monitor monitor) {
    Optional<Monitor> dbMonitorRequest = monitorOrchestrationService
        .getMonitor(monitor.getMetric());
    if (!dbMonitorRequest.isPresent()) {
      throw new IllegalArgumentException("Monitor doesn't exist: " + monitor);
    }
    Monitor dbMonitor = dbMonitorRequest.get();
    for (MonitoringTarget target : monitor.getTargets()) {
      if (!dbMonitor.getTargets().contains(target)) {
        dbMonitor.addTargetsItem(target);
      }
    }
    monitorOrchestrationService.updateMonitor(dbMonitor);
    return dbMonitor;
  }

  @Transactional
  public List<Monitor> getAllMonitors() {
    return monitorOrchestrationService.getAllMonitors();
  }

  @Transactional
  public void checkAndDeleteMonitor(String metric) {
    monitorOrchestrationService.deleteMonitor(metric);
  }

  @Transactional
  public Monitor getMonitor(String metric) {
    Monitor result = monitorOrchestrationService.getMonitor(metric).get();
    if (result == null) {
      throw new IllegalArgumentException("Monitor not found. ");
    }
    return result;
  }


  public Monitor handleNewMonitor(String userId, Monitor newMonitor) {
    //Target
    for (MonitoringTarget mTarget : newMonitor.getTargets()) {
      //handling
      if (!handleMonitorTarget(userId, mTarget, newMonitor)) {
        throw new AssertionError("Error by handling Target: " + mTarget);
      }
    }
    Monitor requestedMonitor = checkAndCreate(newMonitor);
    if (requestedMonitor == null) {
      throw new IllegalArgumentException("Monitor already exists.");
    }
    return requestedMonitor;
  }

  private boolean handleMonitorTarget(String userId, MonitoringTarget target, Monitor monitor) {
    switch (target.getType()) {
      case PROCESS:
        handleProcess(userId, target, monitor);
        break;
      case TASK:
        handleTask(userId, target, monitor);
        break;
      case JOB:
        handleJob(userId, target, monitor);
        break;
      case NODE:
        System.out.println("Handle NODE: " + target);
        handleNode(userId, target, monitor);
        return true;
      case CLOUD:
        handleCloud(userId, target, monitor);
        break;
      default:
        throw new IllegalArgumentException("unkown MonitorTargetType: " + target.getType());
    }
    return false;
  }

  private void handleNode(String userId, MonitoringTarget target, Monitor monitor) {
    LOGGER.debug("Starting handleNode ");
    Node targetNode = monitorHandler.getNodeById(target.getIdentifier(), userId);

    if (!monitorHandler.installVisor(userId, targetNode)) {
      LOGGER.error("Error by installing Visor on Node ", targetNode);
      throw new IllegalStateException("Error by installing Visor");
    }

    if (!monitorHandler.configureVisor(userId, target, targetNode, monitor)) {
      LOGGER.error("Error by configuring Visor on Node ", targetNode);
      throw new IllegalStateException("Error by configuring Visor");
    }

    LOGGER.debug("Finished handleNode");
  }

  private void handleProcess(String userId, MonitoringTarget target, Monitor monitor) {
    final ProcessConverter PROCESS_CONVERTER = ProcessConverter.INSTANCE;
    // only SingleProcess - ClusterProcess not supported
    // getting Process
    final ProcessQueryRequest processQueryRequest = ProcessQueryRequest.newBuilder()
        .setUserId(userId).setProcessId(target.getIdentifier()).build();
    CloudiatorProcess process = null;
    try {
      final ProcessQueryResponse processQueryResponse = processService
          .queryProcesses(processQueryRequest);
      if (processQueryResponse.getProcessesCount() == 0) {
        throw new IllegalStateException("Process not found: " + target);
      }
      if (processQueryResponse.getProcessesCount() > 1) {
        throw new IllegalStateException("More than one Process found: " + target);
      }
      process = PROCESS_CONVERTER
          .applyBack(processQueryResponse.getProcesses(0));

    } catch (ResponseException e) {
      LOGGER.error("handleProcess threw ResponseException: ", e);
      throw new IllegalStateException("Exception while handling Process: ", e);
    }
    //  handling Process on Node
    if (process instanceof SingleProcess) {
      LOGGER.debug("Start handling SingleProcess");
      Node processNode = monitorHandler.getNodeById(((SingleProcess) process).getNode(), userId);

      if (!monitorHandler.installVisor(userId, processNode)) {
        LOGGER.error("Error by installing Visor on Node: " + processNode.name());
        throw new IllegalStateException("Error by installing Visor on Node: " + processNode);
      }

      if (!monitorHandler.configureVisor(userId, target, processNode, monitor)) {
        LOGGER.error("Error by configuring Visor on Node: " + processNode.name());
        throw new IllegalStateException("Error by configuring Visor on Node: " + processNode);
      }
      LOGGER.debug("Finished handling SingleProcess");

    } else if (process instanceof ClusterProcess) {
      //not implemented
      throw new IllegalStateException("ClusterProcess not implemented");
    }
  }

  private void handleTask(String userId, MonitoringTarget target, Monitor monitor) {
  }

  private void handleJob(String userId, MonitoringTarget target, Monitor monitor) {
    //
  }

  private void handleCloud(String userId, MonitoringTarget target, Monitor monitor) {
    // NodeGroups:

  }

  private void monitorupdate(String userId){
    //checking Monitors in Database
    //get AllMonitors()
    List<Monitor>allMonitors = monitorOrchestrationService.getAllMonitors();
    //forEachMontor get NodeById
    for (Monitor monitor: allMonitors) {
      monitorHandler.getNodeById(monitor.getMetric(),userId);
    }


  }

}
