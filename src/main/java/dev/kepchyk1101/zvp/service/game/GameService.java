package dev.kepchyk1101.zvp.service.game;

import dev.kepchyk1101.zvp.service.Service;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public interface GameService extends Service {
  
  @NotNull GameTeam getTeamOf(@NotNull Player player);
  
  void swapTeam(@NotNull Player player, @NotNull GameTeam team);
  
  void join(@NotNull Player player);
  
  void start();
  
  boolean isStarted();
  
}
