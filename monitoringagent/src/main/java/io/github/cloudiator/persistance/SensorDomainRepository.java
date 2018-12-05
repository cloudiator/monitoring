package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.rest.model.Sensor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SensorDomainRepository {

  final static SensorModelConverter SENSOR_MODEL_CONVERTER = new SensorModelConverter();

  private final PullSensorModelRepository pullSensorModelRepository;
  private final PushSensorModelRepository pushSensorModelRepository;

  @Inject
  public SensorDomainRepository(PullSensorModelRepository pullSensorModelRepository,
      PushSensorModelRepository pushSensorModelRepository) {
    this.pullSensorModelRepository = pullSensorModelRepository;
    this.pushSensorModelRepository = pushSensorModelRepository;

  }


  public List<Sensor> findAllSensors() {
    List<Sensor> allSensors = new ArrayList<Sensor>();
    for (PullSensorModel pullSensorModel : pullSensorModelRepository.findAll()) {
      allSensors.add(SENSOR_MODEL_CONVERTER.apply(pullSensorModel));
    }
    for (PushSensorModel pushSensorModel : pushSensorModelRepository.findAll()) {
      allSensors.add(SENSOR_MODEL_CONVERTER.apply(pushSensorModel));
    }
    return allSensors;
  }


  public void saveSensor(SensorModel sensorModel) {
    if (sensorModel instanceof PullSensorModel) {
      pullSensorModelRepository.save((PullSensorModel) sensorModel);
    } else if (sensorModel instanceof PushSensorModel) {
      pushSensorModelRepository.save((PushSensorModel) sensorModel);
    }
  }

  public Optional<SensorModel> findPullSensorByClassName(String className) {
    Optional<PullSensorModel> pullSensorModel = pullSensorModelRepository
        .findPullSensorByClassName(className);
    if (!pullSensorModel.isPresent()) {
      return Optional.empty();
    } else {
      return Optional.of(pullSensorModel.orElse(null));
    }
  }


}
