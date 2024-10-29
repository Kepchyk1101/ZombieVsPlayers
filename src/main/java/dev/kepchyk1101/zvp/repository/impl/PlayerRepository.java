package dev.kepchyk1101.zvp.repository.impl;

import com.j256.ormlite.support.ConnectionSource;
import dev.kepchyk1101.zvp.entity.PlayerEntity;
import dev.kepchyk1101.zvp.repository.BaseRepository;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerRepository extends BaseRepository<PlayerEntity, UUID> {
  
  public PlayerRepository(@NotNull ConnectionSource connectionSource) {
    super(connectionSource);
  }
  
  @Override
  protected @NotNull Class<PlayerEntity> getEntityClass() {
    return PlayerEntity.class;
  }
  
}
