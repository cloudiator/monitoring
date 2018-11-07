package io.github.cloudiator.monitoring.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import com.mchange.util.AssertException;
import io.github.cloudiator.persistance.MonitorDomainRepository;
import java.util.ArrayList;
import java.util.Optional;
import org.cloudiator.messages.Installation.InstallationRequest;
import org.cloudiator.messages.Installation.InstallationResponse;
import org.cloudiator.messaging.SettableFutureResponseCallback;
import org.cloudiator.messaging.services.InstallationRequestService;

public class BasicMonitorOrchestrationService implements MonitorOrchestrationService {

  private final MonitorDomainRepository monitorDomainRepository;


  @Inject
  public BasicMonitorOrchestrationService(MonitorDomainRepository monitorDomainRepository) {
    this.monitorDomainRepository = monitorDomainRepository;
  }


  @Override
  public Monitor createMonitor(Monitor newMonitor) {

    for (MonitoringTarget target : newMonitor.getTargets()) {
      switch (target.getType()) {
        case CLOUD:
          break;
        case NODE:
          /*
          SettableFutureResponseCallback<InstallationResponse, InstallationResponse> installationFuture = SettableFutureResponseCallback
              .create();
          installationRequestService.createInstallationRequestAsync(null, installationFuture);
          installationFuture.get();
          */
          break;
        case JOB:
          break;
        case TASK:
          break;
        case PROCESS:
          break;
        default:
          throw new AssertionError("MonitorTargetTypeError " + target.getType());
      }
    }

    return monitorDomainRepository.addMonitor(newMonitor);

  }

  @Override
  public Iterable<Monitor> getAllMonitors() {
    return monitorDomainRepository.getAllMonitors();
  }

  @Override
  public void updateMonitor(Monitor monitor) {
    checkNotNull(monitor, "Monitor is null.");
    //Not implemented
  }

  @Override
  public void deleteMonitor(Monitor monitor) {

  }

  @Override
  public Optional<Monitor> getMonitor(String monitorMetric) {
    checkNotNull(monitorMetric, "Metric is null");
    final Monitor result = monitorDomainRepository.findMonitorByMetric(monitorMetric);
    if (result == null) {
      return Optional.empty();
    }
    return Optional.of(result);
  }


}
