package io.github.cloudiator.monitoring.domain;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.monitoring.converter.MonitorToVisorMonitorConverter;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
import io.github.cloudiator.persistance.MonitorModel;
import io.github.cloudiator.persistance.MonitorModelConverter;
import io.github.cloudiator.rest.converter.ProcessConverter;
import io.github.cloudiator.rest.model.CloudiatorProcess;
import io.github.cloudiator.rest.model.ClusterProcess;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.rest.model.SingleProcess;
import io.github.cloudiator.domain.Node;
import java.util.List;
import java.util.Map;
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
  private final MonitorToVisorMonitorConverter visorMonitorConverter = MonitorToVisorMonitorConverter.INSTANCE;
  private final MonitorModelConverter monitorModelConverter = MonitorModelConverter.INSTANCE;
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


  public DomainMonitorModel checkAndCreate(DomainMonitorModel monitor, String userid) {
    Optional<MonitorModel> dbMonitor = null;
    dbMonitor = monitorOrchestrationService
        .getMonitor(monitor.getMetric(), userid);
    if (dbMonitor.isPresent()) {
      return null;
    } else {
      return monitorOrchestrationService.createMonitor(monitor, userid);
    }
  }


  public List<DomainMonitorModel> getAllMonitors() {
    return monitorOrchestrationService.getAllMonitors();
  }

  public List<DomainMonitorModel> getAllYourMonitors(String userid) {
    return monitorOrchestrationService.getAllYourMonitors(userid);
  }


  public MonitorModel checkAndDeleteMonitor(String metric, MonitoringTarget target) {
    return monitorOrchestrationService.deleteMonitor(
        metric.concat("+++").concat(target.getType().name()).concat("+++")
            .concat(target.getIdentifier()));
  }


  public DomainMonitorModel getMonitor(String metric, MonitoringTarget target, String userid) {
    MonitorModel result = monitorOrchestrationService
        .getMonitor(metric.concat("+++")
            .concat(target.getType().name().concat("+++").concat(target.getIdentifier())), userid)
        .get();
    if (result == null) {
      throw new IllegalArgumentException("Monitor not found. ");
    }
    return monitorModelConverter.apply(result);
  }


  public synchronized DomainMonitorModel handleNewMonitor(String userId, Monitor newMonitor) {
    //Target
    LOGGER.debug("Handling " + newMonitor.getTargets().size() + " Targets");

    DomainMonitorModel requestedMonitor = null;
    Integer count = 1;
    for (MonitoringTarget mTarget : newMonitor.getTargets()) {

      DomainMonitorModel domainMonitor = new DomainMonitorModel(newMonitor.getMetric(),
          newMonitor.getTargets(), newMonitor.getSensor(), newMonitor.getSinks(),
          newMonitor.getTags());
      LOGGER.debug("Handling Target " + count + " of " + newMonitor.getMetric());
      //handling
      String dbMetric = new String(
          domainMonitor.getMetric() + "+++" + mTarget.getType().name() + "+++" + mTarget
              .getIdentifier());
      domainMonitor.setMetric(dbMetric);

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
    DomainMonitorModel result = checkAndCreate(monitor, userId);
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
            DomainMonitorModel monitorex = monitor;
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

            String dbMetric = new String(
                monitorex.getMetric() + "+++" + monitoringTarget.getType().name() + "+++"
                    + monitoringTarget
                    .getIdentifier());
            MonitorModel dbmonitor = monitorOrchestrationService.getMonitor(dbMetric, user)
                .get();
            dbmonitor.setUuid(visorback.getUuid());
            LOGGER
                .debug(
                    "EDIT-Metric: " + dbmonitor.getMetric() + ", uuid: " + visorback.getUuid());
            monitorOrchestrationService.updateMonitor(dbmonitor);
            LOGGER.debug(
                "EDITED-Metric: " + dbmonitor.getMetric() + ", uuid: " + visorback.getUuid());
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
    final ProcessConverter PROCESS_CONVERTER = ProcessConverter.INSTANCE;
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
      DomainMonitorModel result = checkAndCreate(domainMonitor, userId);
      if (result == null) {
        throw new IllegalArgumentException("Monitor already exists.");
      } else {
        LOGGER.debug("Monitor in DB created");

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
          public void run() {
            if (installMelodicTools) {
              try {
                visorMonitorHandler.installEMSClient(userId, processNode);
              } catch (IllegalStateException e) {
                LOGGER.debug("Exception during EMSInstallation: " + e);
                LOGGER.debug("---");
              } catch (Exception re) {
                LOGGER.debug("Exception while EMSInstallation " + re);
              }
            }
            visorMonitorHandler.installVisor(userId, processNode);
            io.github.cloudiator.visor.rest.model.Monitor visorback = visorMonitorHandler
                .configureVisor(processNode, domainMonitor);
            /* for testing: ignoring target and configures localhost*/
            //visorMonitorHandler.configureVisortest(processNode, domainMonitor);
            /*   --------------------------------------------------    */
            //LOGGER.debug("back: " + visorback.getUuid());
            MonitorModel dbmonitor = monitorOrchestrationService
                .getMonitor(result.getMetric(), userId)
                .get();
            dbmonitor.setUuid(visorback.getUuid());
            //LOGGER.debug("EDIT-metric: " + dbmonitor.getMetric());
            monitorOrchestrationService.updateMonitor(dbmonitor);

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

  private DomainMonitorModel handleJob(String userId, MonitoringTarget target, Monitor
      monitor) {
    //
    return null;
  }

  private DomainMonitorModel handleTask(String userId, MonitoringTarget target, Monitor
      monitor) {
    //
    return null;
  }

  private DomainMonitorModel handleCloud(String userId, MonitoringTarget target, Monitor
      monitor) {
    // NodeGroups:
    return null;
  }

  public DomainMonitorModel updateMonitor(String userId, String metric) {
    //checking Monitor in Database
    MonitorModel result = monitorOrchestrationService.getMonitor(metric, userId).get();

    return null;
  }

  public void deleteMonitor(String userId, String metric, MonitoringTarget target) {
    Node targetNode = null;
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
    //Deleting DBMonitor
    MonitorModel candidate = checkAndDeleteMonitor(metric, target);
    //Stopping VisorInstance
    visorMonitorHandler.deleteVisorMonitor(targetNode, candidate);
  }

}
