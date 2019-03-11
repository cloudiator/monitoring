package io.github.cloudiator.monitoring.domain;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.monitoring.converter.MonitorToVisorMonitorConverter;
import io.github.cloudiator.monitoring.models.DomainMonitorModel;
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
import java.util.concurrent.TimeUnit;
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

  @Inject
  public MonitorManagementService(VisorMonitorHandler visorMonitorHandler,
      BasicMonitorOrchestrationService monitorOrchestrationService, ProcessService processService) {
    this.visorMonitorHandler = visorMonitorHandler;
    this.monitorOrchestrationService = monitorOrchestrationService;
    this.processService = processService;
  }

  @Transactional
  public DomainMonitorModel checkAndCreate(Monitor monitor) {
    Boolean check = false;
    Optional<DomainMonitorModel> dbMonitor = null;
    //monitor.setMetric(dbMetric);
    dbMonitor = monitorOrchestrationService
        .getMonitor(monitor.getMetric());
    if (dbMonitor.isPresent()) {
      LOGGER.debug("found Monitor: " + dbMonitor.get());
      check = true;
    }

    //Optional<DomainMonitorModel> dbMonitor = monitorOrchestrationService.getMonitor(monitor.getMetric());
    if (check) {
      return null;
    } else {
      dbMonitor = Optional.of(monitorOrchestrationService.createMonitor(monitor));
      return dbMonitor.get();
    }
  }

  @Transactional
  public DomainMonitorModel checkMonitor(Monitor monitor) {
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
  public void checkAndDeleteMonitor(String metric, MonitoringTarget target) {
    LOGGER.debug("starting checkAndDelete :" + metric.concat("+++").concat(target.getType().name())
        .concat("+++")
        .concat(target.getIdentifier()));
    monitorOrchestrationService.deleteMonitor(
        metric.concat("+++").concat(target.getType().name()).concat("+++")
            .concat(target.getIdentifier()));
  }

  @Transactional
  public void deleteAll() {
    LOGGER.debug("DeleteAll executed!");
    monitorOrchestrationService.deleteAll();
  }

  @Transactional
  public Monitor getMonitor(String metric, MonitoringTarget target) {
    DomainMonitorModel result = monitorOrchestrationService
        .getMonitor(metric.concat("+++")
            .concat(target.getType().name().concat("+++").concat(target.getIdentifier()))).get();
    if (result == null) {
      throw new IllegalArgumentException("Monitor not found. ");
    }
    return result;
  }


  public DomainMonitorModel handleNewMonitor(String userId, Monitor newMonitor) {
    //Target
    LOGGER.debug("Handling " + newMonitor.getTargets().size() + " Targets");

    DomainMonitorModel requestedMonitor = null;

    Integer count = 1;
    for (MonitoringTarget mTarget : newMonitor.getTargets()) {

        DomainMonitorModel domainMonitor = new DomainMonitorModel(newMonitor.getMetric(),
            newMonitor.getTargets(), newMonitor.getSensor(), newMonitor.getSinks(),
            newMonitor.getTags());
        LOGGER.debug("Handling Target " + count);
        //handling
        String dbMetric = new String(
            domainMonitor.getMetric() + "+++" + mTarget.getType().name() + "+++" + mTarget
                .getIdentifier());
        domainMonitor.setMetric(dbMetric);

        switch (mTarget.getType()) {
          case PROCESS:
            LOGGER.debug("Handle PROCESS: " + mTarget);
            requestedMonitor = handleProcess(userId, mTarget, domainMonitor);
            break;
          case TASK:
            requestedMonitor = handleTask(userId, mTarget, domainMonitor);
            break;
          case JOB:
            requestedMonitor = handleJob(userId, mTarget, domainMonitor);
            break;
          case NODE:
            LOGGER.debug("Handle NODE: " + mTarget);
            requestedMonitor = handleNode(userId, mTarget, domainMonitor);
            break;
          case CLOUD:
            requestedMonitor = handleCloud(userId, mTarget, domainMonitor);
            break;
          default:
            throw new IllegalArgumentException("unkown MonitorTargetType: " + mTarget.getType());
        }
        count++;
        //  TimeUnit.MILLISECONDS.sleep(500);



    }
    return requestedMonitor;
  }

  private DomainMonitorModel handleNode(String userId, MonitoringTarget target,
      DomainMonitorModel monitor) {
    LOGGER.debug("Starting handleNode ");

    Node targetNode = visorMonitorHandler.getNodeById(target.getIdentifier(), userId);

    //writing NodeState into DB Model
    Map tags = monitor.getTags();
    tags.put("NodeState", targetNode.state().name());
    tags.put("IP", targetNode.connectTo().ip());
    monitor.setTags(tags);
    DomainMonitorModel result = checkAndCreate(monitor);
    if (result == null) {
      throw new IllegalArgumentException("Monitor already exists:" + monitor.getMetric());
    } else {
      LOGGER.debug("Monitor in DB created");

      ExecutorService executorService = Executors.newSingleThreadExecutor();
      executorService.execute(new Runnable() {
        public void run() {
          LOGGER.debug("starting asynchronous task");
          try {
            visorMonitorHandler.installEMSClient(userId, targetNode);
          } catch (IllegalStateException e) {
            LOGGER.debug("Exception during EMSInstallation: " + e);
            LOGGER.debug("---");
          }
          visorMonitorHandler.installVisor(userId, targetNode);
          visorMonitorHandler.configureVisor(targetNode, monitor);
          LOGGER.debug("visor install and config done");
        }
      });
      executorService.shutdown();
      LOGGER.debug("Finished handleNode");
      return result;
    }
  }


  private DomainMonitorModel handleProcess(String userId, MonitoringTarget target,
      DomainMonitorModel domainMonitor) {
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

      //writing NodeState into DB Model
      Map tags = domainMonitor.getTags();
      tags.put("NodeState", processNode.state().name());
      tags.put("IP", processNode.connectTo().ip());
      domainMonitor.setTags(tags);
      DomainMonitorModel result = checkAndCreate(domainMonitor);
      if (result == null) {
        throw new IllegalArgumentException("Monitor already exists.");
      } else {
        LOGGER.debug("Monitor in DB created");

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {
          public void run() {
            LOGGER.debug("starting asynchronous task");
            try {
              visorMonitorHandler.installEMSClient(userId, processNode);
            } catch (IllegalStateException e) {
              LOGGER.debug("Exception during EMSInstallation: " + e);
              LOGGER.debug("---");
            } catch (Exception re) {
              LOGGER.debug("Exception while EMSInstallation " + re);
            }
            visorMonitorHandler.installVisor(userId, processNode);
            visorMonitorHandler.configureVisor(processNode, domainMonitor);
            LOGGER.debug("visor install and config done");
          }
        });
        executorService.shutdown();

        LOGGER.debug("Finished handling SingleProcess");
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
    DomainMonitorModel result = monitorOrchestrationService.getMonitor(metric).get();

    return result;
  }

  public DomainMonitorModel getAllVisor(String userId, MonitoringTarget target) {
    Node targetNode = visorMonitorHandler.getNodeById(target.getIdentifier(), userId);
    LOGGER.debug("Got Node ");
    List<io.github.cloudiator.visor.rest.model.Monitor> testresult = visorMonitorHandler
        .getAllVisorMonitors(targetNode);
    LOGGER.debug("got monitors ");

    DomainMonitorModel result = visorMonitorConverter.applyBack(testresult.get(0));
    LOGGER.debug("made output " + result);
    return result;
  }

}
