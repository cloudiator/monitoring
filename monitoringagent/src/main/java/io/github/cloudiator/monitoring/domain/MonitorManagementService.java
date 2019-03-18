package io.github.cloudiator.monitoring.domain;

import com.google.inject.Inject;
import com.google.inject.Singleton;
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
  private final MonitorModelConverter monitorModelConverter = MonitorModelConverter.INSTANCE;

  @Inject
  public MonitorManagementService(VisorMonitorHandler visorMonitorHandler,
      BasicMonitorOrchestrationService monitorOrchestrationService, ProcessService processService) {
    this.visorMonitorHandler = visorMonitorHandler;
    this.monitorOrchestrationService = monitorOrchestrationService;
    this.processService = processService;
  }

  @Transactional
  public MonitorModel persistMonitor(MonitorModel monitorModel) {
    monitorOrchestrationService.persistMonitor(monitorModel);
    return monitorModel;
  }

  @Transactional
  public DomainMonitorModel checkAndCreate(DomainMonitorModel monitor) {
    Optional<DomainMonitorModel> dbMonitor = null;
    dbMonitor = monitorOrchestrationService
        .getMonitor(monitor.getMetric());
    if (dbMonitor.isPresent()) {
      return null;
    } else {
      DomainMonitorModel add = monitorOrchestrationService.createMonitor(monitor);
      return add;
    }
  }


  //@Transactional
  public List<DomainMonitorModel> getAllMonitors() {
    return monitorOrchestrationService.getAllMonitors();
  }

  @Transactional
  public void checkAndDeleteMonitor(String metric, MonitoringTarget target) {
    monitorOrchestrationService.deleteMonitor(
        metric.concat("+++").concat(target.getType().name()).concat("+++")
            .concat(target.getIdentifier()));
  }

  @Transactional
  public void deleteAll() {
    LOGGER.debug("DeleteAll executed!");
    monitorOrchestrationService.deleteAll();
  }

  //@Transactional
  public Monitor getMonitor(String metric, MonitoringTarget target) {
    DomainMonitorModel result = monitorOrchestrationService
        .getMonitor(metric.concat("+++")
            .concat(target.getType().name().concat("+++").concat(target.getIdentifier()))).get();
    if (result == null) {
      throw new IllegalArgumentException("Monitor not found. ");
    }
    return result;
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
      LOGGER.debug("Handling Target of " + newMonitor.getMetric() + "Target: " + count);
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
      //  TimeUnit.MILLISECONDS.sleep(500);

    }
    return requestedMonitor;
  }

  private DomainMonitorModel handleNode(String userId, MonitoringTarget target,
      DomainMonitorModel monitor) {

    Node targetNode = visorMonitorHandler.getNodeById(target.getIdentifier(), userId);

    //writing NodeState into DB Model
    Map tags = monitor.getTags();
    tags.put("NodeState", targetNode.state().name());
    tags.put("IP", targetNode.connectTo().ip());
    tags.put(target.getType().toString(), target.getIdentifier());
    monitor.setTags(tags);
    DomainMonitorModel result = checkAndCreate(monitor);
    if (result == null) {
      throw new IllegalArgumentException("Monitor already exists:" + monitor.getMetric());
    } else {
      LOGGER.debug("Monitor in DB created");

      ExecutorService executorService = Executors.newSingleThreadExecutor();
      executorService.execute(new Runnable() {
        public void run() {
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
      Node processNode = visorMonitorHandler
          .getNodeById(((SingleProcess) process).getNode(), userId);

      //writing NodeState into DB Model
      Map tags = domainMonitor.getTags();
      tags.put("NodeState", processNode.state().name());
      tags.put("IP", processNode.connectTo().ip());
      tags.put(target.getType().toString(), target.getIdentifier());
      domainMonitor.setTags(tags);
      DomainMonitorModel result = checkAndCreate(domainMonitor);
      if (result == null) {
        throw new IllegalArgumentException("Monitor already exists.");
      } else {
        LOGGER.debug("Monitor in DB created");

        ExecutorService executorService = Executors.newSingleThreadExecutor();

        executorService.execute(new Runnable() {
          public void run() {
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
