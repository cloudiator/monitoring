package io.github.cloudiator.monitoring.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class MonitorContext {

  private final Config config;
  private static final String melodicTools = "monitoring.install.melodic.tools";
  private static final String retryAttempts = "monitoring.retry.attempts";
  private static final String minWaitingTime = "monitoring.retry.minwaitingtime";
  private static final String maxWaitingTime = "monitoring.retry.maxwaitingtime";

  public MonitorContext(Config config) {
    this.config = config;
  }

  public boolean installMelodicTools() {
    return config.getBoolean(melodicTools);
  }

  public int retryAttempts() {
    return config.getInt(retryAttempts);
  }

  public int retryMinimalWaitingTime() {
    return config.getInt(minWaitingTime);
  }

  public int retryMaximalWaitingTime() {
    return config.getInt(maxWaitingTime);
  }
}
