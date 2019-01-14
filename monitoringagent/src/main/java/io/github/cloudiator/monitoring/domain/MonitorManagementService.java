package io.github.cloudiator.monitoring.domain;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.rest.model.Monitor;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.rest.model.Node;
import java.util.List;
import java.util.Optional;
import org.cloudiator.messaging.ResponseException;


public class MonitorManagementService {

  private final MonitorHandler monitorHandler;
  private final MonitorOrchestrationService monitorOrchestrationService;

  @Inject
  public MonitorManagementService(MonitorHandler monitorHandler,
      BasicMonitorOrchestrationService monitorOrchestrationService) {
    this.monitorHandler = monitorHandler;
    this.monitorOrchestrationService = monitorOrchestrationService;
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
    Node targetNode;
    targetNode = monitorHandler.installVisor(userId, target.getIdentifier());

    monitorHandler.configureVisor(userId, target, targetNode ,monitor);

  }

  private void handleProcess(String userId, MonitoringTarget target, Monitor monitor) {
    // haben ID: Process - Groups?
  }

  private void handleTask(String userId, MonitoringTarget target, Monitor monitor) {
  }

  private void handleJob(String userId, MonitoringTarget target, Monitor monitor) {
    //
  }

  private void handleCloud(String userId, MonitoringTarget target, Monitor monitor) {
    // NodeGroups:

  }


}
