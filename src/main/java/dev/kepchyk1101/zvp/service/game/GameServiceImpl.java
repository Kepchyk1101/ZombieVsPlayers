package dev.kepchyk1101.zvp.service.game;

import dev.kepchyk1101.zvp.configuration.PluginConfiguration;
import dev.kepchyk1101.zvp.service.player.PlayerService;
import dev.kepchyk1101.zvp.service.title.TitleService;
import dev.kepchyk1101.zvp.service.zombie.ZombieService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GameServiceImpl implements GameService, Listener {
  
  @NotNull Plugin plugin;
  
  @NotNull ZombieService zombieService;
  
  @NotNull PlayerService playerService;
  
  @NotNull TitleService titleService;
  
  @NotNull PluginConfiguration configuration;
  
  @NonFinal
  long lastKnownDayTick;
  
  
  @Override
  public void enable() {
    Bukkit.getOnlinePlayers().forEach(this::join);
    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateZombieEffects, 0L, 100L);
  }
  
  @Override
  public @NotNull GameTeam getTeamOf(@NotNull Player player) {
    if (playerService.isPlayer(player)) {
      return GameTeam.PLAYER;
    } else if (zombieService.isZombie(player)) {
      return GameTeam.ZOMBIE;
    } else {
      throw new RuntimeException("Can't get team of " + player);
    }
  }
  
  @Override
  public void swapTeam(@NotNull Player player, @NotNull GameTeam team) {
    if (team == GameTeam.ZOMBIE && playerService.isPlayer(player)) {
      playerService.quit(player);
      zombieService.join(player);
      return;
    }
    if (team == GameTeam.PLAYER && zombieService.isZombie(player)) {
      zombieService.quit(player);
      playerService.join(player);
    }
  }
  
  @Override
  public void join(@NotNull Player player) {
    if (!zombieService.isZombie(player) && !playerService.isPlayer(player)) {
      playerService.join(player);
      return;
    }
    if (zombieService.isZombie(player)) {
      zombieService.join(player);
      return;
    }
    if (playerService.isPlayer(player)) {
      playerService.join(player);
    }
  }
  
  @EventHandler
  private void on(@NotNull PlayerJoinEvent event) {
    join(event.getPlayer());
  }
  
  @EventHandler
  private void on(@NotNull PlayerRespawnEvent event) {
    Player player = event.getPlayer();
    if (zombieService.isZombie(player)) {
      zombieService.join(player);
    }
  }
  
  @EventHandler
  private void on(@NotNull PlayerDeathEvent event) {
    Player player = event.getEntity();
    if (playerService.isPlayer(player)) {
      playerService.quit(player);
      zombieService.join(player);
    }
    if (playerService.getPlayers().isEmpty()) {
      Bukkit.getOnlinePlayers().forEach(op -> titleService.sendTitleWithTypingEffect(op, "zombiesWonTitle", 2));
    }
  }
  
  private boolean isDayTransition(long currentTime) {
    return currentTime > 5850 && currentTime < 6150;
  }
  
  private boolean isNightTransition(long currentTime) {
    return currentTime > 11850 && currentTime < 12150;
  }
  
  private void applyEffects(Set<PotionEffect> applyEffects, Set<PotionEffect> removeEffects) {
    Bukkit.getScheduler().runTask(plugin, () -> {
      Bukkit.getOnlinePlayers()
        .stream()
        .filter(zombieService::isZombie)
        .forEach(zombie -> {
          removeEffects.stream()
            .map(PotionEffect::getType)
            .forEach(zombie::removePotionEffect);
          applyEffects.stream()
            .filter(effect -> !zombie.hasPotionEffect(effect.getType()))
            .forEach(zombie::addPotionEffect);
        });
    });
  }
  
  private void checkDayCycleAndNotify(long currentTime) {
    if (currentTime < lastKnownDayTick) {
      long daysPassed = Bukkit.getWorld("world").getGameTime() / 24000;
      long daysRemaining = 50 - daysPassed;
      
      if (daysRemaining == 0) {
        if (!playerService.getPlayers().isEmpty()) {
          Bukkit.getOnlinePlayers().forEach(player -> titleService.sendTitleWithTypingEffect(player, "playersWonTitle", 2));
        }
        return;
      }
      
      if (configuration.isShowRemainingDaysTitle()) {
        Bukkit.getOnlinePlayers().forEach(player -> titleService.sendTitleWithTypingEffect(player, "daysRemaining", 2,
          "%days-remaining%", daysRemaining));
      }
    }
    lastKnownDayTick = currentTime;
  }
  
  private void updateZombieEffects() {
    World world = Bukkit.getWorld("world");
    long currentTime = world.getTime();
    if (isDayTransition(currentTime)) {
      applyEffects(configuration.getZombie().getDayEffects(), configuration.getZombie().getNightEffects());
    } else if (isNightTransition(currentTime)) {
      applyEffects(configuration.getZombie().getNightEffects(), configuration.getZombie().getDayEffects());
    }
    checkDayCycleAndNotify(currentTime);
  }
  
}
