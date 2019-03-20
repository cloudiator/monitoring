package io.github.cloudiator.persistance;


import java.util.Optional;

public interface TargetModelRepository extends BaseModelRepository<TargetModel> {

  Optional<TargetModel> getByIdentifierAndType(String identifier, TargetType targetType);


}
