package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;


import com.google.inject.Inject;
import io.github.cloudiator.rest.model.MonitoringTarget;
import io.github.cloudiator.rest.model.MonitoringTarget.TypeEnum;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TargetDomainRepository {

  final static TargetModelConverter TARGET_MODEL_CONVERTER = new TargetModelConverter();

  private final TargetModelRepository targetModelRepository;

  @Inject
  public TargetDomainRepository(TargetModelRepository targetModelRepository) {
    this.targetModelRepository = targetModelRepository;
  }

  public boolean exists(TargetModel targetModel) {
    checkNotNull(targetModel, "TargetModel is null.");
    return targetModelRepository
        .getByIdentifierAndType(targetModel.getIdentifier(), targetModel.getTargetType())
        .isPresent();
  }

  public boolean exists(MonitoringTarget monitoringTarget) {
    checkNotNull(monitoringTarget, "monitoringTarget is null.");
    return targetModelRepository.getByIdentifierAndType(monitoringTarget.getIdentifier(),
        TargetType.valueOf(monitoringTarget.getType().name())).isPresent();
  }

  public Optional<TargetModel> getByIdentifierAndType(String identifier, TargetType targetType) {
    checkNotNull(identifier, "identifier is null.");
    Optional<TargetModel> targetModel = targetModelRepository
        .getByIdentifierAndType(identifier, targetType);
    if (targetModel == null) {
      return Optional.empty();
    }
    return targetModel;
  }

  public void saveTarget(TargetModel targetModel) {
    targetModelRepository.save(targetModel);
  }

  public List<TargetModel> createTargetModelList(List<MonitoringTarget> targets) {
    List<TargetModel> result = new ArrayList<>();
    for (MonitoringTarget monitoringTarget : targets) {
      result.add(
          createTargetModel(TargetType.valueOf(monitoringTarget.getType().name()),
              monitoringTarget.getIdentifier())
      );
    }
    return result;
  }

  public TargetModel createTargetModel(TargetType targetType, String identifier) {
    TargetModel result = new TargetModel(targetType, identifier);
    targetModelRepository.save(result);
    return result;
  }

}
