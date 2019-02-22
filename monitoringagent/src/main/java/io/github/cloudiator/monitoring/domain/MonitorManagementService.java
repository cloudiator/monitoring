package io.github.cloudiator.monitoring.domain;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.messaging.NodeToNodeMessageConverter;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.rest.converter.ProcessConverter;
import io.github.cloudiator.rest.model.CloudiatorProcess;
import io.github.cloudiator.rest.model.ClusterProcess;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.rest.model.SingleProcess;
import io.github.cloudiator.domain.Node;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.cloudiator.messages.Process.ProcessQueryRequest;
import org.cloudiator.messages.Process.ProcessQueryResponse;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class MonitorManagementService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorManagementService.class);

  private final VisorMonitorHandler visorMonitorHandler;
  private final MonitorOrchestrationService monitorOrchestrationService;
  private final ProcessService processService;
  private final NodeToNodeMessageConverter nodeMessageConverter = NodeToNodeMessageConverter.INSTANCE;

  @Inject
  public MonitorManagementService(VisorMonitorHandler visorMonitorHandler,
      BasicMonitorOrchestrationService monitorOrchestrationService, ProcessService processService) {
    this.visorMonitorHandler = visorMonitorHandler;
    this.monitorOrchestrationService = monitorOrchestrationService;
    this.processService = processService;
  }

  @Transactional
  public DomainMonitorModel checkAndCreate(Monitor monitor) {
    Optional<DomainMonitorModel> dbMonitor = monitorOrchestrationService
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
    Optional<DomainMonitorModel> dbMonitorRequest = monitorOrchestrationService
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
  public List<DomainMonitorModel> getAllMonitors() {
    return monitorOrchestrationService.getAllMonitors();
  }

  @Transactional
  public void checkAndDeleteMonitor(String metric) {
    LOGGER.debug("checkAndDelete " + metric);
    // io.github.cloudiator.visor.rest.model.Monitor result= monitorOrchestrationService.getMonitor(metric);

    monitorOrchestrationService.deleteMonitor(metric);
  }

  @Transactional
  public void deleteAll() {
    LOGGER.debug("DeleteAll executed!");
    monitorOrchestrationService.deleteAll();
  }

  @Transactional
  public Monitor getMonitor(String metric) {
    Monitor result = monitorOrchestrationService.getMonitor(metric).get();
    if (result == null) {
      throw new IllegalArgumentException("Monitor not found. ");
    }
    return result;
  }


  public DomainMonitorModel handleNewMonitor(String userId, Monitor newMonitor) {
    //Target
    DomainMonitorModel domainMonitor = new DomainMonitorModel(newMonitor.getMetric(),
        newMonitor.getTargets(), newMonitor.getSensor(), newMonitor.getSinks(),
        newMonitor.getTags());
    LOGGER.debug("Handling " + newMonitor.getTargets().size() + " Targets");
    DomainMonitorModel requestedMonitor = checkAndCreate(newMonitor);
    LOGGER.debug("Monitor in DB created");
    if (requestedMonitor == null) {
      throw new IllegalArgumentException("Monitor already exists.");
    }

    Integer count = 1;
    for (MonitoringTarget mTarget : domainMonitor.getTargets()) {
      LOGGER.debug("Handling Target " + count);
      //handling
      handleMonitorTarget(userId, mTarget, domainMonitor);
      count++;
    }

    return requestedMonitor;
  }

  private void handleMonitorTarget(String userId, MonitoringTarget target,
      DomainMonitorModel monitor) {
    switch (target.getType()) {
      case PROCESS:
        LOGGER.debug("Handle PROCESS: " + target);
        handleProcess(userId, target, monitor);

        break;
      case TASK:
        handleTask(userId, target, monitor);
        break;
      case JOB:
        handleJob(userId, target, monitor);
        break;
      case NODE:
        LOGGER.debug("Handle NODE: " + target);
        handleNode(userId, target, monitor);
        break;
      case CLOUD:
        handleCloud(userId, target, monitor);
        break;
      default:
        throw new IllegalArgumentException("unkown MonitorTargetType: " + target.getType());
    }
  }

  private void handleNode(String userId, MonitoringTarget target, DomainMonitorModel monitor) {
    LOGGER.debug("Starting handleNode ");
    Node targetNode = visorMonitorHandler.getNodeById(target.getIdentifier(), userId);
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    executorService.execute(new Runnable() {
      public void run() {
        LOGGER.debug("starting asynchronous task");
        visorMonitorHandler.installVisor(userId, targetNode);
        visorMonitorHandler.configureVisor(targetNode, monitor);
        LOGGER.debug("visor install and config done");
      }
    });

    executorService.shutdown();

    LOGGER.debug("Finished handleNode");
  }

  private void handleProcess(String userId, MonitoringTarget target, DomainMonitorModel monitor) {
    final ProcessConverter PROCESS_CONVERTER = ProcessConverter.INSTANCE;
    // only SingleProcess - ClusterProcess not supported
    // getting Process
    ProcessQueryRequest processQueryRequest = ProcessQueryRequest.newBuilder()
        .setUserId(userId).setProcessId(target.getIdentifier()).build();
    CloudiatorProcess process = null;
    try {
      ProcessQueryResponse processQueryResponse = processService
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
      Node processNode = visorMonitorHandler
          .getNodeById(((SingleProcess) process).getNode(), userId);

      ExecutorService executorService = Executors.newSingleThreadExecutor();

      executorService.execute(new Runnable() {
        public void run() {
          LOGGER.debug("starting asynchronous task");
          visorMonitorHandler.installVisor(userId, processNode);
          visorMonitorHandler.configureVisor(processNode, monitor);
          LOGGER.debug("visor install and config done");
        }
      });
      executorService.shutdown();

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

  public DomainMonitorModel updateMonitor(String userId, String metric) {
    //checking Monitor in Database
    DomainMonitorModel result = monitorOrchestrationService.getMonitor(metric).get();

    return result;
  }

}
