package io.github.cloudiator.monitoring.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class MonitorContext {

  private final Config config;

  public MonitorContext(Config config) {
    this.config = config;
    config.checkValid(ConfigFactory.defaultReference(), "db");
  }

  public boolean installMelodicTools() {
    return config.getBoolean(melodicTools);
  }


}
