package io.github.cloudiator.monitoring.domain;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import io.github.cloudiator.monitoring.converter.MonitorToVisorMonitorConverter;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.persistance.MonitorModel;
import io.github.cloudiator.persistance.MonitorModelConverter;
import io.github.cloudiator.rest.converter.JobConverter;
import io.github.cloudiator.rest.converter.ProcessConverter;
import io.github.cloudiator.rest.converter.ScheduleConverter;
import io.github.cloudiator.rest.converter.TaskConverter;
import io.github.cloudiator.rest.model.CloudiatorProcess;
import io.github.cloudiator.rest.model.ClusterProcess;
import io.github.cloudiator.rest.model.Job;
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
  private final JobService jobService;
  private final TaskConverter TASK_CONVERTER = new TaskConverter();
  private final MonitorToVisorMonitorConverter VISOR_MONITOR_CONVERTER = MonitorToVisorMonitorConverter.INSTANCE;
  private final MonitorModelConverter MONITOR_MODEL_CONVERTER = MonitorModelConverter.INSTANCE;
  private final JobConverter JOB_CONVERTER = new JobConverter();
  private final ProcessConverter PROCESS_CONVERTER = ProcessConverter.INSTANCE;
  private final ScheduleConverter SCHEDULE_CONVERTER = new ScheduleConverter();
  private final boolean installMelodicTools;


  @Inject
  public MonitorManagementService(VisorMonitorHandler visorMonitorHandler,
      BasicMonitorOrchestrationService monitorOrchestrationService, ProcessService processService,
      JobService jobService,
      @Named("melodicTools") boolean installMelodicTools) {
    this.visorMonitorHandler = visorMonitorHandler;
    this.monitorOrchestrationService = monitorOrchestrationService;
    this.processService = processService;
    this.installMelodicTools = installMelodicTools;
    this.jobService = jobService;
  }

  private String generateDBMetric(String monitormetric, String targetId, TypeEnum targetType) {
    return monitormetric.concat("+++").concat(targetType.name()).concat("+++")
        .concat(targetId);
  }


  public DomainMonitorModel checkAndCreate(String dbMetric, DomainMonitorModel monitor,
      String userid) {
    Optional<DomainMonitorModel> dbMonitor = monitorOrchestrationService
        .getMonitor(dbMetric, userid);
    if (dbMonitor.isPresent()) {
      return null;
    } else {
      return monitorOrchestrationService.createMonitor(dbMetric, monitor, userid);
    }
  }


  public List<DomainMonitorModel> getAllMonitors() {
    return monitorOrchestrationService.getAllMonitors();
  }

  public List<DomainMonitorModel> getAllYourMonitors(String userid) {
    return monitorOrchestrationService.getAllYourMonitors(userid);
  }


  public DomainMonitorModel checkAndDeleteMonitor(String metric, MonitoringTarget target) {
    DomainMonitorModel result = monitorOrchestrationService
        .deleteMonitor(generateDBMetric(metric, target.getIdentifier(), target.getType()));
    return result;
  }


  public DomainMonitorModel getMonitor(String metric, MonitoringTarget target, String userid) {
    DomainMonitorModel result = monitorOrchestrationService
        .getMonitor(generateDBMetric(metric, target.getIdentifier(), target.getType()), userid)
        .get();
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
      domainMonitor.setMetric(newMonitor.getMetric());
    }
    return requestedMonitor;
  }

  public DomainMonitorModel handleNode(String userId, MonitoringTarget target,
      DomainMonitorModel monitor) {

    Node targetNode = visorMonitorHandler.getNodeById(userId, target.getIdentifier());

    //writing NodeState into DB Model
    Map tags = monitor.getTags();
    tags.put("NodeState", targetNode.state().name());
    tags.put("IP", targetNode.connectTo().ip());
    tags.put(target.getType().toString(), target.getIdentifier());
    monitor.setTags(tags);
    DomainMonitorModel result = checkAndCreate(
        generateDBMetric(monitor.getMetric(), target.getIdentifier(), target.getType()), monitor,
        userId);
    if (result == null) {
      throw new IllegalArgumentException("Monitor already exists:" + monitor.getMetric());
    } else {
      LOGGER.debug("Monitor in DB created");

      ExecutorService executorService = Executors.newSingleThreadExecutor();
      executorService.execute(new Runnable() {
        public void run() {
          try {
            String user = userId;
            Node targetnode = targetNode;
            DomainMonitorModel monitorex = result;
            MonitoringTarget monitoringTarget = target;

            if (installMelodicTools) {
              try {
                visorMonitorHandler.installEMSClient(userId, targetNode);

              } catch (IllegalStateException e) {
                LOGGER.debug("Exception during EMSInstallation: " + e);
                LOGGER.debug("---");
              }
            }
            visorMonitorHandler.installVisor(user, targetnode);
            io.github.cloudiator.visor.rest.model.Monitor visorback = visorMonitorHandler
                .configureVisor(targetnode, monitorex);

            String dbMetric = generateDBMetric(monitorex.getMetric(),
                monitoringTarget.getIdentifier(), monitoringTarget.getType());

            DomainMonitorModel dbmonitor = monitorOrchestrationService.getMonitor(dbMetric, user)
                .get();
            dbmonitor.setUuid(visorback.getUuid());
            LOGGER
                .debug(
                    "EDIT-Metric: " + dbmonitor.getMetric() + ", uuid: " + visorback.getUuid());
            monitorOrchestrationService.updateMonitor(dbMetric, dbmonitor, user);
            LOGGER.debug("visor install and config done");
          } catch (Throwable t) {
            LOGGER.error("Unexpected Exception", t);
          }
        }
      });
      executorService.shutdown();
      return result;
    }
  }

  public CloudiatorProcess getProcessFromTarget(String userId, MonitoringTarget target) {

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
    return process;
  }

  public DomainMonitorModel handleProcess(String userId, MonitoringTarget target,
      DomainMonitorModel domainMonitor) {
    CloudiatorProcess process = getProcessFromTarget(userId, target);
    //  handling Process on Node
    if (process instanceof SingleProcess) {
      Node processNode = visorMonitorHandler
          .getNodeById(userId, ((SingleProcess) process).getNode());

      //writing NodeState into DB Model
      Map tags = domainMonitor.getTags();
      tags.put("NodeState", processNode.state().name());
      tags.put("IP", processNode.connectTo().ip());
      tags.put(target.getType().toString(), target.getIdentifier());
      domainMonitor.setTags(tags);
      DomainMonitorModel result = checkAndCreate(
          generateDBMetric(domainMonitor.getMetric(), target.getIdentifier(), target.getType()),
          domainMonitor, userId);
      if (result == null) {
        throw new IllegalArgumentException("Monitor already exists.");
      } else {
        LOGGER.debug("Monitor in DB created");

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
          public void run() {
            String user = userId;
            Node targetnode = processNode;
            DomainMonitorModel monitorex = domainMonitor;
            MonitoringTarget monitoringTarget = target;
            if (installMelodicTools) {
              try {
                visorMonitorHandler.installEMSClient(user, targetnode);
              } catch (IllegalStateException e) {
                LOGGER.debug("Exception during EMSInstallation: " + e);
                LOGGER.debug("---");
              } catch (Exception re) {
                LOGGER.debug("Exception while EMSInstallation " + re);
              }
            }
            visorMonitorHandler.installVisor(user, targetnode);
            io.github.cloudiator.visor.rest.model.Monitor visorback = visorMonitorHandler
                .configureVisor(targetnode, monitorex);
            String dbMetric = generateDBMetric(monitorex.getMetric(),
                monitoringTarget.getIdentifier(), monitoringTarget.getType());

            DomainMonitorModel dbmonitor = monitorOrchestrationService.getMonitor(dbMetric, user)
                .get();
            dbmonitor.setUuid(visorback.getUuid());
            monitorOrchestrationService.updateMonitor(dbMetric, dbmonitor, user);
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
      result = checkAndCreate(
          generateDBMetric(monitor.getMetric(), target.getIdentifier(), target.getType()), monitor,
          userId);
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
      DomainMonitorModel result = checkAndCreate(
          generateDBMetric(monitor.getMetric(), target.getIdentifier(), target.getType()), monitor,
          userId);
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

  public DomainMonitorModel updateMonitorFromRest(Monitor monitor, String userId) {
    //checking for related Monitors in Database
    boolean updateSensor = false;
    boolean updateTags = false;
    boolean updateTarget = false;
    boolean updateDatasinks = false;
    List<DomainMonitorModel> dbList = monitorOrchestrationService
        .getMonitorsWithSameMetric(monitor.getMetric(), userId);
    if (dbList.isEmpty()) {
      //ERROR: check your input
      return null;
    }
    DomainMonitorModel restMonitor = (DomainMonitorModel) monitor;
    DomainMonitorModel dbMonitor = dbList.get(0);
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
    /* TODO
    for (DomainMonitorModel mModel : dbList) {
      monitorOrchestrationService
          .updateMonitorFromRest(mModel, restMonitor, updateSensor, updateTags, updateTarget,
              updateDatasinks);
    }
    */
    return restMonitor;
  }

  public void deleteMonitor(String userId, String metric, MonitoringTarget target) {
    Node targetNode;
    switch (target.getType()) {
      case NODE:
        targetNode = visorMonitorHandler.getNodeById(userId, target.getIdentifier());
        break;
      case PROCESS:
        CloudiatorProcess process = getProcessFromTarget(userId, target);
        targetNode = visorMonitorHandler
            .getNodeById(userId, ((SingleProcess) process).getNode());
        break;
      default:
        throw new IllegalStateException("unkown TargetType: " + target.getType());
    }
    DomainMonitorModel candidate;
    Optional<DomainMonitorModel> dbresult = monitorOrchestrationService
        .getMonitor(generateDBMetric(metric, target.getIdentifier(), target.getType()), userId);
    if (!dbresult.isPresent()) {
      LOGGER.debug("no Result in Database!");
      throw new IllegalArgumentException("Got no Monitor from DB to delete");
    }
    candidate = dbresult.get();
    if (candidate.getUuid().isEmpty() || candidate.getUuid().equals("0")) {
      LOGGER.debug("No VisorUuid found in Monitor: " + metric);
      LOGGER.debug("Can't delete Visor.");
    } else {
      LOGGER.debug("stopping Visor and remove from DB");
      //Stopping VisorInstance
      visorMonitorHandler.deleteVisorMonitor(targetNode, candidate);
    }
    //Deleting DBMonitor
    candidate = checkAndDeleteMonitor(metric, target);
    LOGGER.debug("Monitor deleted.");
  }

  /**********************
   * Event Handling
   *********************/


  public void handeldeletedNode(Node node, String userId) {
    List<DomainMonitorModel> affectedMonitors = monitorOrchestrationService
        .getMonitorsOnTarget(node.id(), userId);
    System.out.println("affected: " + affectedMonitors.toString());
    for (DomainMonitorModel dMonitor : affectedMonitors) {
      monitorOrchestrationService
          .deleteMonitor(generateDBMetric(dMonitor.getMetric(), node.id(), TypeEnum.NODE));
      System.out.println("delete Monitor");
    }

  }

  public void handledeletedProcess(CloudiatorProcess cloudiatorProcess, String userId) {
    SingleProcess singleProcess = (SingleProcess) cloudiatorProcess;
    List<DomainMonitorModel> affectedMonitos = monitorOrchestrationService
        .getMonitorsOnTarget(singleProcess.getId(), userId);
    System.out.println(affectedMonitos.size() + "Monitors affected!");
    for (DomainMonitorModel dmonitor : affectedMonitos) {
      monitorOrchestrationService.deleteMonitor(
          generateDBMetric(dmonitor.getMetric(), singleProcess.getId(), TypeEnum.PROCESS));
    }

  }


}
