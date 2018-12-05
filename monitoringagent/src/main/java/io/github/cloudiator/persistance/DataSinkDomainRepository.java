package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class DataSinkDomainRepository {

  private final DataSinkModelRepository dataSinkModelRepository;


  @Inject
  public DataSinkDomainRepository(DataSinkModelRepository dataSinkModelRepository) {
    this.dataSinkModelRepository = dataSinkModelRepository;
  }


}

