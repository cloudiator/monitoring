package io.github.cloudiator.monitoring.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

public class MonitorContext {

  private final Config config;
  private static final String melodicTools = "monitoring.install.melodic.tools";

  public MonitorContext(Config config) {
    this.config = config;
  }

  public boolean installMelodicTools() {
    return config.getBoolean(melodicTools);
  }


}
