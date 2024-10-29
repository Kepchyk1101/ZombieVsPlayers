package dev.kepchyk1101.zvp.service.zombie;

import dev.kepchyk1101.zvp.service.Service;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public interface ZombieService extends Service {
  
  boolean isZombie(@NotNull Player player);
  
  void join(@NotNull Player player);
  
  void setFireOnZombies();
  
  void setFireOnZombie(@NotNull Player player);
  
  void quit(@NotNull Player player);
  
  @NotNull Set<@NotNull UUID> getZombies();
}
