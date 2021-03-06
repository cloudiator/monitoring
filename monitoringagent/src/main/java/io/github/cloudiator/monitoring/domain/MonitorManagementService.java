package io.github.cloudiator.monitoring.domain;

import co.paralleluniverse.concurrent.util.ScheduledSingleThreadExecutor;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.name.Named;

import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.monitoring.models.TargetState;

import io.github.cloudiator.persistance.TargetType;
import io.github.cloudiator.rest.converter.ProcessConverter;
import io.github.cloudiator.rest.converter.ScheduleConverter;
import io.github.cloudiator.rest.model.CloudiatorProcess;
import io.github.cloudiator.rest.model.ClusterProcess;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.rest.model.MonitoringTarget.TypeEnum;
import io.github.cloudiator.rest.model.Schedule;
import io.github.cloudiator.rest.model.SingleProcess;
import io.github.cloudiator.domain.Node;
import io.github.cloudiator.visor.rest.ApiException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.cloudiator.messages.Process.ProcessQueryRequest;
import org.cloudiator.messages.Process.ProcessQueryResponse;
import org.cloudiator.messages.Process.ScheduleQueryRequest;
import org.cloudiator.messages.Process.ScheduleQueryResponse;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.ProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorManagementService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorManagementService.class);

  private final MonitorHandler monitorHandler;
  private final MonitorOrchestrationService monitorOrchestrationService;
  private final ProcessService processService;
  private final ProcessConverter PROCESS_CONVERTER = ProcessConverter.INSTANCE;
  private final ScheduleConverter SCHEDULE_CONVERTER = new ScheduleConverter();
  private final MonitorQueueController MONITOR_QUEUE_CONTROLLER;
  // -> MonitorHandler
  private final boolean installMelodicTools;
  //
  private static final ExecutorService monitorExecutor = Executors.newFixedThreadPool(10);

  static {
    MoreExecutors.addDelayedShutdownHook(monitorExecutor, 100, TimeUnit.MILLISECONDS);
    LOGGER.info("Monitoring initialized");
  }


  @Inject
  public MonitorManagementService(MonitorHandler monitorHandler,
      BasicMonitorOrchestrationService monitorOrchestrationService, ProcessService processService,
      @Named("melodicTools") boolean installMelodicTools,
      MonitorQueueController monitorQueueController) {
    this.monitorHandler = monitorHandler;
    this.monitorOrchestrationService = monitorOrchestrationService;
    this.processService = processService;
    this.installMelodicTools = installMelodicTools;
    this.MONITOR_QUEUE_CONTROLLER = monitorQueueController;
  }


  public DomainMonitorModel checkAndCreate(DomainMonitorModel monitor,
      String userId) {
    if (monitorOrchestrationService.existingMonitor(monitor, userId)) {
      return null;
    } else {
      return monitorOrchestrationService.createMonitor(monitor, userId);
    }
  }

  private DomainMonitorModel createDBMonitor(String userId, MonitoringTarget target,
      DomainMonitorModel domainMonitorModel) {
    //writing Tags
    Map tags = domainMonitorModel.getTags();
    domainMonitorModel.setTags(tags);
    domainMonitorModel.setOwnTargetType(target.getType());
    domainMonitorModel.setOwnTargetId(target.getIdentifier());
    domainMonitorModel.setOwnTargetState(TargetState.PENDING);
    domainMonitorModel.setOwner(userId);

    //creating DBEntry
    DomainMonitorModel result;
    if (monitorOrchestrationService.existingMonitor(domainMonitorModel, userId)) {
      result = null;
    } else {
      result = monitorOrchestrationService.createMonitor(domainMonitorModel, userId);

    }
    return result;
  }

  public List<DomainMonitorModel> getAllMonitors() {
    return monitorOrchestrationService.getAllMonitors();
  }

  public List<DomainMonitorModel> getAllYourMonitors(String userid) {
    List<DomainMonitorModel> result = monitorOrchestrationService.getAllYourMonitors(userid);
    VisorRetryer.retry(100, 200, 2,
        () -> monitorOrchestrationService.getMonitorCount());
    return result;
  }


  public DomainMonitorModel getMonitor(String metric, MonitoringTarget monitoringTarget,
      String userId) {
    DomainMonitorModel result = monitorOrchestrationService
        .getMonitor(metric, monitoringTarget, userId).get();
    if (result == null) {
      throw new IllegalArgumentException("Monitor not found. ");
    }
    return result;
  }


  public DomainMonitorModel handleNewMonitor(String userId, Monitor newMonitor) {
    //Target
    DomainMonitorModel requestedMonitor = null;
    Integer count = 1;
    for (MonitoringTarget mTarget : newMonitor.getTargets()) {
      DomainMonitorModel domainMonitor = new DomainMonitorModel(newMonitor.getMetric(),
          mTarget.getType(), mTarget.getIdentifier(),
          newMonitor.getTargets(), newMonitor.getSensor(), newMonitor.getSinks(),
          newMonitor.getTags());
      LOGGER.debug(
          newMonitor.getMetric() + ": handling Target " + count + " of " + newMonitor.getTargets()
              .size());

      //DBMonitor erstellen
      DomainMonitorModel result = createDBMonitor(userId, mTarget, domainMonitor);
      if (result == null) {
        throw new IllegalArgumentException("Monitor already exists:" + domainMonitor.getMetric());
      } else {
        LOGGER.debug(mTarget.getType() + "Monitor in DB created");
      }
      requestedMonitor = result;

      //handling Type
      switch (mTarget.getType()) {
        case PROCESS:
          handleProcess(userId, mTarget, result);
          break;
        case TASK:
          handleTask(userId, mTarget, result);
          break;
        case JOB:
          handleJob(userId, mTarget, result);
          break;
        case NODE:
          handleNode(userId, mTarget, result);
          break;
        case CLOUD:
          handleCloud(userId, mTarget, result);
          break;
        default:
          throw new IllegalArgumentException("unkown MonitorTargetType: " + mTarget.getType());
      }
      count++;
    }
    return requestedMonitor;
  }

  public DomainMonitorModel handleNode(String userId, MonitoringTarget target,
      DomainMonitorModel monitor) {

    monitor.setOwnTargetType(target.getType());
    monitor.setOwnTargetId(target.getIdentifier());
    // monitorExecutor.execute(() -> monitorHandler.handleNodeMonitor(userId, monitor));
    boolean test = MONITOR_QUEUE_CONTROLLER.handleMonitorRequest(target.getIdentifier(), monitor);
    LOGGER.debug("QueueControllerAction: " + test);

    return monitor;
  }


  public CloudiatorProcess getProcessFromTarget(String userId, String targetId) {

    ProcessQueryRequest processQueryRequest = ProcessQueryRequest.newBuilder()
        .setUserId(userId).setProcessId(targetId).build();
    CloudiatorProcess process = null;
    try {
      ProcessQueryResponse processQueryResponse = processService
          .queryProcesses(processQueryRequest);
      if (processQueryResponse.getProcessesCount() == 0) {
        throw new IllegalStateException("Process not found: " + targetId);
      }
      if (processQueryResponse.getProcessesCount() > 1) {
        throw new IllegalStateException("More than one Process found: " + targetId);
      }
      process = PROCESS_CONVERTER
          .applyBack(processQueryResponse.getProcesses(0));
    } catch (ResponseException e) {
      LOGGER.error("handleProcess threw ResponseException: ", e);
      throw new IllegalStateException("Exception while handling Process: ", e);
    }
    return process;
  }

  public DomainMonitorModel handleProcess(String userId, MonitoringTarget target,
      DomainMonitorModel domainMonitor) {
    CloudiatorProcess process = getProcessFromTarget(userId, target.getIdentifier());
    //  handling Monitor for ProcessNode
    if (process instanceof SingleProcess) {
      domainMonitor.addTagItem("Endpoint:", process.getEndpoint());
      domainMonitor.setOwnTargetState(TargetState.valueOf(process.getState().name()));
      //updating ProcessMonitor
      monitorOrchestrationService.updateMonitor(domainMonitor, userId);

      target.setType(TypeEnum.NODE);
      target.setIdentifier(((SingleProcess) process).getNode());
      //creating NodeMonitor
      DomainMonitorModel result = createDBMonitor(userId, target, domainMonitor);
      if (result == null) {
        throw new IllegalArgumentException("Monitor already exists.");
      } else {
        LOGGER.debug(target.getType() + "Monitor in DB created");

        //Handling Visor on Node
        // monitorExecutor.execute(() -> monitorHandler.handleNodeMonitor(userId, result));
        MONITOR_QUEUE_CONTROLLER.handleMonitorRequest(result.getOwnTargetId(), result);


      }
      return result;
    } else if (process instanceof ClusterProcess) {
      //not implemented
      throw new IllegalStateException("ClusterProcess not implemented");
    }
    //should never be reached
    return null;
  }


  /*
  - get all schedules
  - search for jobId in schedules
  - monitor all processes
   */
  private DomainMonitorModel handleJob(String userId, MonitoringTarget target, DomainMonitorModel
      monitor) {
    List<Schedule> allSchedules = new ArrayList<>();
    Schedule scheduleToHandle = null;
    List<CloudiatorProcess> processesToHandle;
    DomainMonitorModel result = null;

    try {
      final ScheduleQueryResponse scheduleQueryResponse = processService.querySchedules(
          ScheduleQueryRequest.newBuilder().setUserId(userId).build());
      allSchedules = scheduleQueryResponse.getSchedulesList().stream()
          .map(schedule -> SCHEDULE_CONVERTER.apply(schedule)).collect(
              Collectors.toList());
      scheduleToHandle = allSchedules.stream()
          .filter(schedule -> schedule.getJob().equals(target.getIdentifier())).findAny()
          .orElse(null);
      if (scheduleToHandle == null) {
        LOGGER.debug("No schedule of requested job found. JobId: " + target.getIdentifier());
        //throw Exception
      }
      // writing DBMonitor
      result = checkAndCreate(monitor, userId);
      if (result == null) {
        throw new IllegalArgumentException("Monitor already exists.");
      } else {
        LOGGER.debug("Monitor in DB created");
      }

      processesToHandle = scheduleToHandle.getProcesses();
      for (CloudiatorProcess cProcess : processesToHandle) {
        MonitoringTarget ctarget = new MonitoringTarget().identifier(cProcess.getId())
            .type(TypeEnum.PROCESS);
        handleProcess(userId, ctarget, monitor);
      }

      LOGGER.debug("Full Job handled");

    } catch (ResponseException re) {
      LOGGER.debug("Exception while getting jobs.");
    }
    return result;
  }

  /*
  - target identifier build as {jobID}/{taskname} to be unique
  -search for job and task in schedule
  - monitor all related processes
   */

  private DomainMonitorModel handleTask(String userId, MonitoringTarget
      target, DomainMonitorModel
      monitor) {
    List<Schedule> allSchedules = new ArrayList<>();
    Schedule scheduleToHandle = null;
    List<CloudiatorProcess> processesToHandle;

    try {
      final ScheduleQueryResponse scheduleQueryResponse = processService.querySchedules(
          ScheduleQueryRequest.newBuilder().setUserId(userId).build());
      allSchedules = scheduleQueryResponse.getSchedulesList().stream()
          .map(schedule -> SCHEDULE_CONVERTER.apply(schedule)).collect(
              Collectors.toList());
      scheduleToHandle = allSchedules.stream()
          .filter(schedule -> schedule.getJob().equals(target.getIdentifier())).findAny()
          .orElse(null);
      if (scheduleToHandle == null) {
        LOGGER.debug("No schedule of requested job found. JobId: " + target.getIdentifier());
        //throw Exception
      }
      // writing DBMonitor
      DomainMonitorModel result = checkAndCreate(monitor, userId);
      if (result == null) {
        throw new IllegalArgumentException("Monitor already exists.");
      } else {
        LOGGER.debug("Monitor in DB created");
      }

      processesToHandle = scheduleToHandle.getProcesses();
      for (CloudiatorProcess cProcess : processesToHandle) {
        MonitoringTarget ctarget = new MonitoringTarget().identifier(cProcess.getId())
            .type(TypeEnum.PROCESS);
        handleProcess(userId, ctarget, monitor);
      }
    } catch (ResponseException re) {
      LOGGER.debug("Exception while getting jobs.");
    }
    return null;
  }

  private DomainMonitorModel handleCloud(String userId, MonitoringTarget target, Monitor
      monitor) {
    // NOT IMPLEMENTED NOT USED
    return null;
  }

  /* NO USED FUNCTION REST-PUT is not used */
  public DomainMonitorModel updateMonitorFromRest(Monitor monitor, String userId) {

    return (DomainMonitorModel) monitor;
  }

  public void deleteMonitor(String userId, String metric, MonitoringTarget monitoringTarget) {
    Optional<DomainMonitorModel> dbResult = monitorOrchestrationService
        .getMonitor(metric, monitoringTarget, userId);
    if (!dbResult.isPresent()) {
      LOGGER.debug("no Result in Database!");
      throw new IllegalArgumentException("Got no Monitor from DB to delete");
    }
    DomainMonitorModel candidate = dbResult.get();

    LOGGER.debug("Deleting Monitor '" + candidate.getOwnTargetType() + ": " + candidate.getMetric()
        + "' TargetState: " + candidate
        .getOwnTargetState());
    String nodeIp;
    Node targetNode = null;
    switch (candidate.getOwnTargetType()) {
      case PROCESS:
        //ONLY SINGLEPROCESS
        CloudiatorProcess process = getProcessFromTarget(userId, candidate.getOwnTargetId());
        if (((SingleProcess) process).getNode().isEmpty()) {
          LOGGER.debug("no related Node in Process");
        } else {
          try {
            targetNode = monitorHandler.getNodeById(userId, ((SingleProcess) process).getNode());
            LOGGER.debug("deleting related NodeMonitor");
            MonitoringTarget nodeTarget = new MonitoringTarget().type(TypeEnum.NODE)
                .identifier(targetNode.id());
            monitorExecutor.execute(() -> deleteMonitor(userId, metric, nodeTarget));
          } catch (AssertionError as) {
            LOGGER.debug("Node not present - continue deleting ProcessMonitor");
          }
        }
        break;
      case NODE:
        if (candidate.getOwnTargetState() != TargetState.DELETED) {
          targetNode = monitorHandler.getNodeById(userId, candidate.getOwnTargetId());
          nodeIp = targetNode.connectTo().ip();
          if (candidate.getUuid().isEmpty() || candidate.getUuid().equals("0")) {
            LOGGER.debug("No VisorUuid found in Monitor: " + metric);
            LOGGER.debug("Removing Monitor from DB");
          } else {
            LOGGER.debug("stopping Visor");
            //Stopping VisorInstance
            try {

              Integer visorStatusResponse = VisorRetryer.retry(1000, 2000, 5,
                  () -> monitorHandler.deleteVisorMonitor(nodeIp, candidate));
              LOGGER.debug("visorStatusResponse by deletingMonitor: " + visorStatusResponse);
            } catch (IllegalStateException ill) {
              LOGGER.debug(
                  "IllegalStateException: VisorMonitor not found - continue deleting DbEntry");
            } catch (Exception e) {
              LOGGER.debug("Exception: " + e);
            }
          }
        }
        break;
      case JOB:
        break;
      case TASK:
        break;
      case CLOUD:
        break;
      default:
        throw new IllegalArgumentException(
            "unkown TargetType: " + candidate.getOwnTargetType());
    }
    //Deleting DBMonitor
    monitorOrchestrationService.deleteMonitor(candidate, userId);
    LOGGER.debug("Monitor deleted.");
  }

  public void checkMonitorStatus() {
    int monitorcount = monitorOrchestrationService.getMonitorCount();
    LOGGER.info("Total Number of Monitors: " + monitorcount);
    int queueMapSizue = MONITOR_QUEUE_CONTROLLER.getQueueMapSize();
    LOGGER.info("QueueMapSize: " + queueMapSizue);
    int max = 50;
    int roundnumber = 1;
    int place = 1;
    /*
    do {
      roundnumber = (monitorcount > max) ? max : monitorcount;
      List<DomainMonitorModel> monitors = monitorOrchestrationService
          .getNumberedMonitors(1, roundnumber);


    } while (roundnumber == monitorcount);
    */

  }

  /**********************
   * Event Handling
   *********************/

  public void handleEvent(TypeEnum targetEnum, String targetId, TargetState targetState) {
    //checking for ralated Monitors
    List<DomainMonitorModel> relatedMonitors = monitorOrchestrationService.getMonitorsOnTarget(
        TargetType.valueOf(targetEnum.toString()), targetId);

    //updating
    if (relatedMonitors == null || relatedMonitors.isEmpty()) {
      //do nothing
    } else {
      LOGGER.debug("Eventhandling causes updating " + relatedMonitors.size() + " Monitors");
      monitorOrchestrationService
          .updateTargetStateInMonitors(TargetType.valueOf(targetEnum.toString()), targetId,
              targetState);

    }
  }

}
