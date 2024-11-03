package dev.kepchyk1101.zvp.service.player;

import dev.kepchyk1101.zvp.configuration.PluginConfiguration;
import dev.kepchyk1101.zvp.entity.PlayerEntity;
import dev.kepchyk1101.zvp.repository.impl.PlayerRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlayerServiceImpl implements PlayerService, Listener {
  
  @NotNull PlayerRepository repository;
  
  @NotNull PluginConfiguration configuration;
  
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
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "skin clear " + player.getName());
    PluginConfiguration.Zombie zombie = configuration.getZombie();
    Stream.of(
        zombie.getDayEffects(),
        zombie.getNightEffects(),
        zombie.getFoodEffects()
      )
      .flatMap(m -> m.values().stream())
      .flatMap(Set::stream)
      .map(PotionEffect::getType)
      .collect(Collectors.toSet())
      .forEach(player::removePotionEffect);
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
