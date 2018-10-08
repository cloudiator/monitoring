package io.github.cloudiator.persistance;

import io.github.cloudiator.persistance.TargetModel.TargetEnum;
import java.util.Optional;

public interface TargetModelRepository extends ModelRepository<TargetModel> {

  Optional<TargetModel> getByIdentifierAndType(String identifier, TargetEnum targetEnum);




}
