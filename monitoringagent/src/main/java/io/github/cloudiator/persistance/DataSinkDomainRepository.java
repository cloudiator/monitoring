package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.inject.Inject;
import io.github.cloudiator.rest.model.DataSink;
import java.util.ArrayList;
import java.util.List;

public class DataSinkDomainRepository {

  final static DataSinkModelConverter DATA_SINK_MODEL_CONVERTER = DataSinkModelConverter.INSTANCE;

  private final DataSinkModelRepository dataSinkModelRepository;

  @Inject
  public DataSinkDomainRepository(DataSinkModelRepository dataSinkModelRepository) {
    this.dataSinkModelRepository = dataSinkModelRepository;
  }

  public void save(DataSink dataSink) {
    checkNotNull(dataSink, "datasink is null");
    saveAndGet(dataSink);
  }

  DataSinkModel saveAndGet(DataSink dataSink) {
    checkNotNull(dataSink, "datasink is null");
    final DataSinkModel model = createModel(dataSink);
    dataSinkModelRepository.save(model);
    return model;
  }

  DataSinkModel createModel(DataSink dataSink) {
    DataSinkModel result = new DataSinkModel(dataSink.getType().name(),
        dataSink.getConfiguration());
    return result;
  }

  public List<DataSinkModel> createDataSinkModelList(List<DataSink> dataSinks) {
    List<DataSinkModel> resultList = new ArrayList<>();
    for (DataSink datasink : dataSinks) {
      resultList.add(
          saveAndGet(datasink)
      );
    }
    return resultList;
  }

}
