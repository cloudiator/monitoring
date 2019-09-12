package io.github.cloudiator.monitoring.domain;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import io.github.cloudiator.monitoring.converter.MonitorToVisorMonitorConverter;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.monitoring.models.TargetState;
import io.github.cloudiator.persistance.StateType;
import io.github.cloudiator.rest.converter.JobConverter;
import io.github.cloudiator.rest.converter.ProcessConverter;
import io.github.cloudiator.rest.converter.ScheduleConverter;
import io.github.cloudiator.rest.converter.TaskConverter;
import io.github.cloudiator.rest.model.CloudiatorProcess;
import io.github.cloudiator.rest.model.ClusterProcess;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.rest.model.MonitoringTarget.TypeEnum;
import io.github.cloudiator.rest.model.Schedule;
import io.github.cloudiator.rest.model.SingleProcess;
import io.github.cloudiator.domain.Node;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.cloudiator.messages.Job.JobQueryRequest;
import org.cloudiator.messages.Job.JobQueryResponse;
import org.cloudiator.messages.Process.ProcessQueryRequest;
import org.cloudiator.messages.Process.ProcessQueryResponse;
import org.cloudiator.messages.Process.ScheduleQueryRequest;
import org.cloudiator.messages.Process.ScheduleQueryResponse;
import org.cloudiator.messages.Task.TaskQueryRequest;
import org.cloudiator.messages.Task.TaskQueryResponse;
import org.cloudiator.messages.entities.JobEntities;
import org.cloudiator.messaging.ResponseException;
import org.cloudiator.messaging.services.JobService;
import org.cloudiator.messaging.services.ProcessService;
import org.cloudiator.messaging.services.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorManagementService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorManagementService.class);

  private final VisorMonitorHandler visorMonitorHandler;
  private final MonitorOrchestrationService monitorOrchestrationService;
  private final ProcessService processService;
  private final ProcessConverter PROCESS_CONVERTER = ProcessConverter.INSTANCE;
  private final ScheduleConverter SCHEDULE_CONVERTER = new ScheduleConverter();
  private final boolean installMelodicTools;


  @Inject
  public MonitorManagementService(VisorMonitorHandler visorMonitorHandler,
      BasicMonitorOrchestrationService monitorOrchestrationService, ProcessService processService,
      @Named("melodicTools") boolean installMelodicTools) {
    this.visorMonitorHandler = visorMonitorHandler;
    this.monitorOrchestrationService = monitorOrchestrationService;
    this.processService = processService;
    this.installMelodicTools = installMelodicTools;
  }


  public DomainMonitorModel checkAndCreate(DomainMonitorModel monitor,
      String userId) {
    if (monitorOrchestrationService.existingMonitor(monitor, userId)) {
      return null;
    } else {
      return monitorOrchestrationService.createMonitor(monitor, userId);
    }
  }

  //TODO aufräumen
  private DomainMonitorModel createDBMonitor(String userId, MonitoringTarget target,
      DomainMonitorModel domainMonitorModel) {
    //writing Tags
    Map tags = domainMonitorModel.getTags();
    domainMonitorModel.setTags(tags);
    domainMonitorModel.setOwnTargetType(target.getType());
    domainMonitorModel.setOwnTargetId(target.getIdentifier());

    //creating DBEntry
    DomainMonitorModel result;
    if (monitorOrchestrationService.existingMonitor(domainMonitorModel, userId)) {
      result = null;
    } else {
      result = monitorOrchestrationService.createMonitor(domainMonitorModel, userId);
      result.addTagItem("ownTarget: ",
          result.getOwnTargetType().toString() + ": " + result.getOwnTargetId());
    }
    return result;
  }

  public List<DomainMonitorModel> getAllMonitors() {
    return monitorOrchestrationService.getAllMonitors();
  }

  public List<DomainMonitorModel> getAllYourMonitors(String userid) {
    return monitorOrchestrationService.getAllYourMonitors(userid);
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

      //handling Type
      switch (mTarget.getType()) {
        case PROCESS:
          requestedMonitor = handleProcess(userId, mTarget, domainMonitor);
          break;
        case TASK:
          requestedMonitor = handleTask(userId, mTarget, domainMonitor);
          break;
        case JOB:
          requestedMonitor = handleJob(userId, mTarget, domainMonitor);
          break;
        case NODE:
          requestedMonitor = handleNode(userId, mTarget, domainMonitor);
          break;
        case CLOUD:
          requestedMonitor = handleCloud(userId, mTarget, domainMonitor);
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
    DomainMonitorModel result = createDBMonitor(userId, target, monitor);
    if (result == null) {
      throw new IllegalArgumentException("Monitor already exists:" + monitor.getMetric());
    } else {
      LOGGER.debug("Monitor in DB created");

      ExecutorService executorService = Executors.newSingleThreadExecutor();
      executorService.execute(new Runnable() {
        public void run() {
          try {
            String threadUser = userId;
            DomainMonitorModel threadDomainMonitor = result;
            Node threadNode = visorMonitorHandler
                .getNodeById(userId, threadDomainMonitor.getOwnTargetId());
            threadDomainMonitor.addTagItem("NodeIP: ", threadNode.connectTo().ip());

            if (installMelodicTools) {
              try {
                visorMonitorHandler.installEMSClient(threadUser, threadNode);

              } catch (IllegalStateException e) {
                LOGGER.debug("Exception during EMSInstallation: " + e);
                LOGGER.debug("---");
              }
            }
            visorMonitorHandler.installVisor(threadUser, threadNode);
            io.github.cloudiator.visor.rest.model.Monitor visorback = visorMonitorHandler
                .configureVisor(threadNode, threadDomainMonitor);
            threadDomainMonitor.setUuid(visorback.getUuid());
            monitorOrchestrationService.updateMonitor(threadDomainMonitor, threadUser);
            threadDomainMonitor.setOwnTargetState(TargetState.valueOf(threadNode.state().name()));
            monitorOrchestrationService.updateTargetState(threadDomainMonitor);
            LOGGER.debug("Visor config done and Monitor updated");
          } catch (Throwable t) {
            LOGGER.error("Unexpected Exception", t);
          }
        }
      });
      executorService.shutdown();
      return result;
    }
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
    //  handling Process on Node
    if (process instanceof SingleProcess) {
      DomainMonitorModel result = createDBMonitor(userId, target, domainMonitor);
      if (result == null) {
        throw new IllegalArgumentException("Monitor already exists.");
      } else {
        LOGGER.debug("Monitor in DB created");

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
          public void run() {
            String threadUser = userId;
            DomainMonitorModel threadDomainMonitor = result;
            Node threadNode = visorMonitorHandler
                .getNodeById(userId, ((SingleProcess) process).getNode());
            threadDomainMonitor.addTagItem("ProcessNode: ", threadNode.name());
            threadDomainMonitor.addTagItem("NodeIP: ", threadNode.connectTo().ip());
            threadDomainMonitor.addTagItem("NodeState: ", threadNode.state().name());
            if (installMelodicTools) {
              try {
                visorMonitorHandler.installEMSClient(threadUser, threadNode);
              } catch (IllegalStateException e) {
                LOGGER.debug("Exception during EMSInstallation: " + e);
                LOGGER.debug("---");
              } catch (Exception re) {
                LOGGER.debug("Exception while EMSInstallation " + re);
              }
            }
            visorMonitorHandler.installVisor(threadUser, threadNode);
            io.github.cloudiator.visor.rest.model.Monitor visorback = visorMonitorHandler
                .configureVisor(threadNode, threadDomainMonitor);
            threadDomainMonitor.setUuid(visorback.getUuid());
            monitorOrchestrationService.updateMonitor(threadDomainMonitor, threadUser);
            LOGGER.debug("visor install and config done");
          }
        });
        executorService.shutdown();
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

  private DomainMonitorModel handleTask(String userId, MonitoringTarget target, DomainMonitorModel
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
    // NodeGroups:
    return null;
  }

  /* NO USED FUNCTION REST-PUT is not used */
  public DomainMonitorModel updateMonitorFromRest(Monitor monitor, String userId) {
    /*
    //checking for related Monitors in Database
    boolean updateSensor = false;
    boolean updateTags = false;
    boolean updateTarget = false;
    boolean updateDatasinks = false;
    List<String> dbList = monitorOrchestrationService
        .getMonitorsWithSameMetric(monitor.getMetric(), userId);
    if (dbList.isEmpty()) {
      //ERROR: check your input
      return null;
    }
    DomainMonitorModel restMonitor = (DomainMonitorModel) monitor;
    DomainMonitorModel dbMonitor = monitorOrchestrationService.getMonitor(dbList.get(0), userId)
        .get();
    if (!restMonitor.getSensor().equals(dbMonitor.getSensor())) {
      updateSensor = true;
    }
    if (!restMonitor.getTags().equals(dbMonitor.getTags())) {
      updateTags = true;
    }
    if (!restMonitor.getTargets().equals(dbMonitor.getTargets())) {
      updateTarget = true;
    }
    if (!restMonitor.getSinks().equals(dbMonitor.getSinks())) {
      updateDatasinks = true;
    }

    for (String mModel : dbList) {
      monitorOrchestrationService
          .updateMonitorFromRest(mModel, userId, restMonitor, updateSensor, updateTags,
              updateTarget,
              updateDatasinks);
    }
    */
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
    String nodeIp;
    if (candidate.getTags().containsKey("IP")) {
      nodeIp = candidate.getTags().get("IP");
    } else if (candidate.getTags().containsKey("NodeIP")) {
      nodeIp = candidate.getTags().get("NodeIP");
    } else {
      Node targetNode = null;
      switch (candidate.getOwnTargetType()) {
        case PROCESS:
          CloudiatorProcess process = getProcessFromTarget(userId, candidate.getOwnTargetId());
          targetNode = visorMonitorHandler
              .getNodeById(userId, ((SingleProcess) process).getNode());
          break;
        case NODE:
          targetNode = visorMonitorHandler.getNodeById(userId, candidate.getOwnTargetId());
          break;
        case JOB:
          break;
        case TASK:
          break;
        case CLOUD:
          break;
        default:
          throw new IllegalArgumentException("unkown TargetType: " + candidate.getOwnTargetType());
      }
      nodeIp = targetNode.connectTo().ip();
    }
    if (candidate.getUuid().isEmpty() || candidate.getUuid().equals("0")) {
      LOGGER.debug("No VisorUuid found in Monitor: " + metric);
      LOGGER.debug("Can't delete Visor. Removing Monitor from DB");
    } else {
      LOGGER.debug("stopping Visor");
      //Stopping VisorInstance
      Integer visorStatusResponse = VisorRetryer.retry(1000, 2000, 5,
          () -> visorMonitorHandler.deleteVisorMonitor(nodeIp, candidate));
    }
    //Deleting DBMonitor
    monitorOrchestrationService.deleteMonitor(candidate, userId);
    LOGGER.debug("Monitor deleted.");
  }

  /**********************
   * Event Handling
   *********************/


  public void handeldeletedNode(Node node, String userId) {
    List<DomainMonitorModel> affectedMonitors = monitorOrchestrationService
        .getMonitorsOnTarget(node.id(), userId);
    LOGGER.debug("affected Monitors: " + affectedMonitors.size());
    for (DomainMonitorModel dMonitor : affectedMonitors) {
      dMonitor.setOwnTargetState(TargetState.DELETED);
      monitorOrchestrationService.updateTargetState(dMonitor);
    }
  }

  public void handledeletedProcess(CloudiatorProcess cloudiatorProcess, String userId) {
    SingleProcess singleProcess = (SingleProcess) cloudiatorProcess;
    List<DomainMonitorModel> affectedMonitors = monitorOrchestrationService
        .getMonitorsOnTarget(singleProcess.getId(), userId);
    LOGGER.debug("affected Monitors: " + affectedMonitors.size());
    for (DomainMonitorModel dbMonitor : affectedMonitors) {
      dbMonitor.setOwnTargetState(TargetState.DELETED);
      monitorOrchestrationService.updateTargetState(dbMonitor);
    }
  }


}
