package io.github.cloudiator.persistance;

import java.util.Optional;

public interface PullSensorModelRepository extends BaseModelRepository<PullSensorModel> {

  Optional<PullSensorModel> findPullSensorByClassName(String className);

}
