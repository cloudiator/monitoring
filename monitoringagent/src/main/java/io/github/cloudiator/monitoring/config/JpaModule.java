package io.github.cloudiator.monitoring.config;

import com.google.inject.AbstractModule;
import com.google.inject.persist.jpa.JpaPersistModule;
import io.github.cloudiator.persistance.DataSinkModelRepository;
import io.github.cloudiator.persistance.DataSinkModelRepositoryJpa;
import io.github.cloudiator.persistance.IntervalModelRepository;
import io.github.cloudiator.persistance.IntervalModelRepositoryJpa;
import io.github.cloudiator.persistance.MonitorModelRepository;
import io.github.cloudiator.persistance.MonitorModelRepositoryJpa;
import io.github.cloudiator.persistance.PullSensorModelRepository;
import io.github.cloudiator.persistance.PullSensorModelRepositoryJpa;
import io.github.cloudiator.persistance.PushSensorModelRepository;
import io.github.cloudiator.persistance.PushSensorModelRepositoryJpa;
import io.github.cloudiator.persistance.TargetModelRepository;
import io.github.cloudiator.persistance.TargetModelRepositoryJpa;
import io.github.cloudiator.util.JpaContext;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by daniel on 31.05.17.
 */
public class JpaModule extends AbstractModule {

  private final String jpaUnit;
  private final JpaContext jpaContext;

  public JpaModule(String jpaUnit, JpaContext jpaContext) {
    this.jpaUnit = jpaUnit;
    this.jpaContext = jpaContext;
  }

  @Override
  protected void configure() {

    install(buildPersistModule());


    bind(MonitorModelRepository.class).to(MonitorModelRepositoryJpa.class);
    bind(PullSensorModelRepository.class).to(PullSensorModelRepositoryJpa.class);
    bind(PushSensorModelRepository.class).to(PushSensorModelRepositoryJpa.class);
    bind(DataSinkModelRepository.class).to(DataSinkModelRepositoryJpa.class);
    bind(TargetModelRepository.class).to(TargetModelRepositoryJpa.class);
    bind(IntervalModelRepository.class).to(IntervalModelRepositoryJpa.class);

  }

  private JpaPersistModule buildPersistModule() {
    final JpaPersistModule jpaPersistModule = new JpaPersistModule(jpaUnit);
    Map<String, String> config = new HashMap<>();
    config.put("hibernate.dialect", jpaContext.dialect());
    config.put("javax.persistence.jdbc.driver", jpaContext.driver());
    config.put("javax.persistence.jdbc.url", jpaContext.url());
    config.put("javax.persistence.jdbc.user", jpaContext.user());
    config.put("javax.persistence.jdbc.password", jpaContext.password());
    jpaPersistModule.properties(config);
    return jpaPersistModule;
  }
}
