package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import io.github.cloudiator.monitoring.domain.MonitoringTarget;
import io.github.cloudiator.persistance.TargetModel.TargetEnum;

public class TargetDomainRepository {

  final static MonitorModelConverter MONITOR_MODEL_CONVERTER = new MonitorModelConverter();
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
        TargetEnum.valueOf(monitoringTarget.getType().name())).isPresent();
  }

  public TargetModel getByIdentifierAndType(String identifier, TargetEnum targetEnum) {
    checkNotNull(identifier, "identifier is null.");
    TargetModel targetModel = targetModelRepository.getByIdentifierAndType(identifier, targetEnum)
        .orElse(null);
    if (targetModel == null) {
      return null;
    }
    return targetModel;
  }

  public void saveTarget(TargetModel targetModel) {
    targetModelRepository.save(targetModel);
  }


}
