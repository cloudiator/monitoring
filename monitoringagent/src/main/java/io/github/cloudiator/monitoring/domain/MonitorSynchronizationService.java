package io.github.cloudiator.monitoring.domain;

import co.paralleluniverse.concurrent.util.ScheduledSingleThreadExecutor;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import io.github.cloudiator.monitoring.messaging.CreateMonitorListener;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.cloudiator.messaging.MessageInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorSynchronizationService implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateMonitorListener.class);
  private final MessageInterface messageInterface;
  private final MonitorManagementService monitorManagementService;
  private static final ScheduledExecutorService databaseExecutor = new ScheduledSingleThreadExecutor();

  static {
    MoreExecutors.addDelayedShutdownHook(databaseExecutor, 100, TimeUnit.MILLISECONDS);
    LOGGER.info("Database Synchronization initialized");
  }

  @Inject
  public MonitorSynchronizationService(MessageInterface messageInterface,
      MonitorManagementService monitorManagementService) {
    this.messageInterface = messageInterface;
    this.monitorManagementService = monitorManagementService;
  }

  @Override
  public void run() {
    LOGGER.info("starting MonitorStatusChecks");
    databaseExecutor
        .scheduleAtFixedRate(() -> monitorManagementService.checkMonitorStatus(), 1, 15,
            TimeUnit.MINUTES);
  }
}
