package io.github.cloudiator.persistance;


import java.util.List;
import javax.annotation.Nullable;

/**
 * basic Repository for Monitoring BaseModel same as in common only separated
 */
@Deprecated
interface BaseModelRepository<T extends BaseModel> {

  @Nullable
  T findById(Long id);

  void delete(T t);

  void save(T t);

  List<T> findAll();
}
