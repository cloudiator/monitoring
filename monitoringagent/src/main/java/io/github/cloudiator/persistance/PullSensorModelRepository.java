package io.github.cloudiator.persistance;

import java.util.Optional;

public interface PullSensorModelRepository extends ModelRepository<PullSensorModel> {

  Optional<PullSensorModel> findPullSensorByClassName(String className);

}
