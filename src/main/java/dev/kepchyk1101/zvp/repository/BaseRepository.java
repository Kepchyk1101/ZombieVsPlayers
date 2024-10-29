package dev.kepchyk1101.zvp.repository;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
public abstract class BaseRepository<T, ID> {
  
  @NotNull ConnectionSource connectionSource;
  
  @NotNull Dao<T, ID> dao;
  
  @SneakyThrows
  public BaseRepository(@NotNull ConnectionSource connectionSource) {
    this.connectionSource = connectionSource;
    this.dao = DaoManager.createDao(connectionSource, getEntityClass());
    TableUtils.createTableIfNotExists(connectionSource, getEntityClass());
  }
  
  @NotNull
  protected abstract Class<T> getEntityClass();
  
  @Nullable
  @SneakyThrows
  public T findById(@NotNull ID id) {
    return dao.queryForId(id);
  }
  
  @SneakyThrows
  public List<T> findAll() {
    return dao.queryForAll();
  }
  
  @SneakyThrows
  public void save(@NotNull T entity) {
    dao.createOrUpdate(entity);
  }
  
  @SneakyThrows
  public void saveAll(Collection<@NotNull T> entities) {
    entities.forEach(this::save);
  }
  
  @SneakyThrows
  public void deleteById(@NotNull ID id) {
    dao.deleteById(id);
  }
  
  @SneakyThrows
  public void deleteAll() {
    TableUtils.clearTable(connectionSource, getEntityClass());
  }
  
}
