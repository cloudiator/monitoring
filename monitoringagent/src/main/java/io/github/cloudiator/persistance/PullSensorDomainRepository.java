package io.github.cloudiator.persistance;

import com.google.inject.Inject;
import io.github.cloudiator.rest.model.Interval;
import java.util.List;
import java.util.Map;

public class PullSensorDomainRepository {

  private final PullSensorModelRepository pullSensorModelRepository;
  private final IntervalModelRepository intervalModelRepository;

  @Inject
  public PullSensorDomainRepository(PullSensorModelRepository pullSensorModelRepository,
      IntervalModelRepository intervalModelRepository) {
    this.pullSensorModelRepository = pullSensorModelRepository;
    this.intervalModelRepository = intervalModelRepository;
  }

  public PullSensorModel createPullSensor(String classname, Interval interval, Map configuration) {

    IntervalModel intervalModel = new IntervalModel(Unit.valueOf(interval.getUnit().name()),
        interval.getPeriod());
    intervalModelRepository.save(intervalModel);

    PullSensorModel result = new PullSensorModel(classname, configuration, intervalModel);


    pullSensorModelRepository.save(result);

    return result;
  }

  public List<PullSensorModel> findAll() {
    return pullSensorModelRepository.findAll();
  }

  public void save(PullSensorModel pullSensorModel) {
    intervalModelRepository.save(pullSensorModel.getInterval());
    pullSensorModelRepository.save(pullSensorModel);
  }


}
