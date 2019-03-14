package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.rest.model.PullSensor;
import io.github.cloudiator.rest.model.Sensor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SensorDomainRepository {

  final static SensorModelConverter SENSOR_MODEL_CONVERTER = new SensorModelConverter();

  private final PullSensorDomainRepository pullSensorDomainRepository;
  private final PushSensorModelRepository pushSensorModelRepository;

  @Inject
  public SensorDomainRepository(PushSensorModelRepository pushSensorModelRepository,
      PullSensorDomainRepository pullSensorDomainRepository) {
    this.pushSensorModelRepository = pushSensorModelRepository;
    this.pullSensorDomainRepository = pullSensorDomainRepository;

  }

  public List<Sensor> findAllSensors() {
    List<Sensor> allSensors = new ArrayList<Sensor>();
    for (PullSensorModel pullSensorModel : pullSensorDomainRepository.findAll()) {
      allSensors.add(SENSOR_MODEL_CONVERTER.apply(pullSensorModel));
    }
    for (PushSensorModel pushSensorModel : pushSensorModelRepository.findAll()) {
      allSensors.add(SENSOR_MODEL_CONVERTER.apply(pushSensorModel));
    }
    return allSensors;
  }

  public PushSensorModel createPushSensor(Integer port) {
    PushSensorModel result = new PushSensorModel(port);
    pushSensorModelRepository.save(result);
    return result;
  }

  public PullSensorModel createPullSensor(PullSensor sensor) {
    return pullSensorDomainRepository
        .createPullSensor(sensor.getClassName(), sensor.getInterval(), sensor.getConfiguration());
  }

  public void saveSensor(SensorModel sensorModel) {
    if (sensorModel instanceof PullSensorModel) {
      pullSensorDomainRepository.save((PullSensorModel) sensorModel);
    } else if (sensorModel instanceof PushSensorModel) {
      pushSensorModelRepository.save((PushSensorModel) sensorModel);
    }
  }

}
