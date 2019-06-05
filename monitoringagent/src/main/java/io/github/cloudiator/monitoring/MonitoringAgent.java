package io.github.cloudiator.monitoring;


import com.google.inject.Guice;
import com.google.inject.Injector;
import io.github.cloudiator.monitoring.config.JpaModule;
import io.github.cloudiator.monitoring.config.MonitorConfigModule;
import io.github.cloudiator.monitoring.config.MonitorContext;
import de.uniulm.omi.cloudiator.util.configuration.Configuration;
import io.github.cloudiator.monitoring.messaging.CreateMonitorListener;
import io.github.cloudiator.monitoring.messaging.DeleteMonitorListener;
import io.github.cloudiator.monitoring.messaging.GetMonitorListener;
import io.github.cloudiator.monitoring.messaging.MonitorQueryListener;
import io.github.cloudiator.monitoring.messaging.NodeEventListener;
import io.github.cloudiator.monitoring.messaging.UpdateMonitorListener;
import io.github.cloudiator.util.JpaContext;
import org.cloudiator.messaging.kafka.KafkaContext;
import org.cloudiator.messaging.kafka.KafkaMessagingModule;
import org.cloudiator.messaging.services.MessageServiceModule;

public class MonitoringAgent {

  private final static Injector injector = Guice
      .createInjector(new MonitorConfigModule(new MonitorContext(Configuration.conf())),
          new KafkaMessagingModule(new KafkaContext(Configuration.conf())),
          new MessageServiceModule(),
          new JpaModule("defaultPersistenceUnit", new JpaContext(Configuration.conf())));

  public static void main(String[] args) {

    final CreateMonitorListener createMonitorListener = injector
        .getInstance(CreateMonitorListener.class);
    createMonitorListener.run();
    final DeleteMonitorListener deleteMonitorListener = injector
        .getInstance(DeleteMonitorListener.class);
    deleteMonitorListener.run();
    final MonitorQueryListener monitorQueryListener = injector
        .getInstance(MonitorQueryListener.class);
    monitorQueryListener.run();
    final UpdateMonitorListener updateMonitorListener = injector
        .getInstance(UpdateMonitorListener.class);
    updateMonitorListener.run();
    final GetMonitorListener getMonitorListener = injector.getInstance(GetMonitorListener.class);
    getMonitorListener.run();
    final NodeEventListener nodeEventListener = injector.getInstance(NodeEventListener.class);
    nodeEventListener.run();
  }
}