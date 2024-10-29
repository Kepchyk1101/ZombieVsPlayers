package dev.kepchyk1101.zvp.service.player;

import dev.kepchyk1101.zvp.service.Service;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public interface PlayerService extends Service {
  
  boolean isPlayer(@NotNull Player player);
  
  void join(@NotNull Player player);
  
  void quit(@NotNull Player player);
  
  @NotNull Set<@NotNull UUID> getPlayers();
  
}
