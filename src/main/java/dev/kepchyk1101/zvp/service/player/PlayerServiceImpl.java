package dev.kepchyk1101.zvp.service.player;

import dev.kepchyk1101.zvp.entity.PlayerEntity;
import dev.kepchyk1101.zvp.repository.impl.PlayerRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlayerServiceImpl implements PlayerService, Listener {
  
  @NotNull PlayerRepository repository;
  
  @NotNull Set<UUID> players = new HashSet<>();
  
  @Override
  public void enable() {
    players.addAll(
      repository.findAll()
        .stream()
        .map(PlayerEntity::getUuid)
        .collect(Collectors.toSet())
    );
  }
  
  @Override
  public void disable() {
    repository.deleteAll();
    repository.saveAll(
      players.stream()
        .map(PlayerEntity::new)
        .collect(Collectors.toSet())
    );
    players.clear();
  }
  
  @Override
  public boolean isPlayer(@NotNull Player player) {
    return players.contains(player.getUniqueId());
  }
  
  @Override
  public void join(@NotNull Player player) {
    players.add(player.getUniqueId());
  }
  
  @Override
  public void quit(@NotNull Player player) {
    players.remove(player.getUniqueId());
  }
  
  @Override
  public @NotNull Set<@NotNull UUID> getPlayers() {
    return Set.copyOf(players);
  }
  
}
