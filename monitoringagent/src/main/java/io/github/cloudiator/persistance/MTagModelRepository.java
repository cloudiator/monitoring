package io.github.cloudiator.persistance;

import java.util.Optional;
@Deprecated
public interface MTagModelRepository extends ModelRepository<MTagModel> {

  public Optional<MTagModel> findByKeyValuePair(String tagKey, String value);

}
