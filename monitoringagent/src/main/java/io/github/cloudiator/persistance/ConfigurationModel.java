package io.github.cloudiator.persistance;

import javax.persistence.Column;
import javax.persistence.ManyToOne;

public class ConfigurationModel extends Model{

  @Column(nullable = false)
  private String configKey;

  @Column(nullable = false)
  private String configValue;

  @ManyToOne()
  private Model configurationOwner;

}
