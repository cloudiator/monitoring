package io.github.cloudiator.monitoring.config;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import io.github.cloudiator.monitoring.Init;
import io.github.cloudiator.monitoring.domain.BasicMonitorOrchestrationService;
import io.github.cloudiator.monitoring.domain.MonitorManagementService;
import io.github.cloudiator.monitoring.domain.MonitorOrchestrationService;
import org.cloudiator.messaging.services.InstallationRequestService;
import org.cloudiator.messaging.services.NodeService;

public class MonitorConfigModule extends AbstractModule {

  private final MonitorContext monitorContext;

  public MonitorConfigModule(MonitorContext monitorContext) {
    this.monitorContext = monitorContext;
  }

  @Override
  protected void configure() {
    bind(Init.class).asEagerSingleton();
    bind(MonitorOrchestrationService.class).to(BasicMonitorOrchestrationService.class);
    bindConstant().annotatedWith(Names.named("melodicTools"))
        .to(monitorContext.installMelodicTools());
  }

}
