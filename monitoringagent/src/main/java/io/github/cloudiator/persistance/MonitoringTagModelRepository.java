package io.github.cloudiator.persistance;

import java.util.Optional;

public interface MonitoringTagModelRepository extends ModelRepository<MonitoringTagModel> {

  public Optional<MonitoringTagModel> findByKeyValuePair(String tagKey, String value);

}
