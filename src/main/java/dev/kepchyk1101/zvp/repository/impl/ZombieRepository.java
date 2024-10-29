package dev.kepchyk1101.zvp.repository.impl;

import com.j256.ormlite.support.ConnectionSource;
import dev.kepchyk1101.zvp.entity.ZombieEntity;
import dev.kepchyk1101.zvp.repository.BaseRepository;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ZombieRepository extends BaseRepository<ZombieEntity, UUID> {
  
  public ZombieRepository(@NotNull ConnectionSource connectionSource) {
    super(connectionSource);
  }
  
  @Override
  protected @NotNull Class<ZombieEntity> getEntityClass() {
    return ZombieEntity.class;
  }
  
}
