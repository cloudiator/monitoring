package io.github.cloudiator.monitoring;

import com.google.inject.Inject;
import com.google.inject.persist.PersistService;

public class Init implements Runnable {

  private final PersistService persistService;

  @Inject
  public Init(PersistService persistService) {
    this.persistService = persistService;
    System.out.println("Hibernate-Version " + org.hibernate.Version.getVersionString());
    run();
  }

  @Override
  public void run() {
    this.persistService.start();
  }

}
