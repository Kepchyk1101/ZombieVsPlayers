package dev.kepchyk1101.zvp.service.game;

import dev.kepchyk1101.zvp.configuration.PluginConfiguration;
import dev.kepchyk1101.zvp.exception.GameAlreadyStarted;
import dev.kepchyk1101.zvp.service.player.PlayerService;
import dev.kepchyk1101.zvp.service.title.TitleService;
import dev.kepchyk1101.zvp.service.zombie.ZombieService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GameServiceImpl implements GameService, Listener {
  
  @SuppressWarnings("DataFlowIssue")
  @NotNull World world = Bukkit.getWorld("world");
  
  @NotNull Plugin plugin;
  
  @NotNull ZombieService zombieService;
  
  @NotNull PlayerService playerService;
  
  @NotNull TitleService titleService;
  
  @NotNull PluginConfiguration configuration;
  
  @NonFinal
  long lastKnownDayTick;
  
  @NonFinal
  boolean dayFreeze = false;
  
  @NonFinal
  boolean judgment = false;
  
  @NonFinal
  boolean playersWon = false;
  
  @NonFinal
  boolean started = false;
  
  @Override
  public void enable() {
    Bukkit.getOnlinePlayers().forEach(this::join);
    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateZombieEffects, 0L, 60L);
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
  
  @Override
  public void start() {
    if (started) {
      throw new GameAlreadyStarted("Game already started!");
    }
    started = true;
    titleService.broadcastTitle("gameStarted", 2);
  }
  
  @Override
  public boolean isStarted() {
    return started;
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
    return currentTime > configuration.getDayStartsFrom() && currentTime < configuration.getDayStartsTo();
  }
  
  private boolean isNightTransition(long currentTime) {
    return currentTime > configuration.getNightStartsFrom() && currentTime < configuration.getNightStartsTo();
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
      long daysPassed = world.getFullTime() / 24000;
      long daysRemaining = 7 - daysPassed;
      
      if (daysPassed + 1 >= 6) {
        Set<PotionEffectType> allNightEffects = configuration.getZombie()
          .getNightEffects()
          .values()
          .stream()
          .flatMap(Collection::stream)
          .map(PotionEffect::getType)
          .collect(Collectors.toSet());
        Bukkit.getScheduler().runTask(plugin, () -> {
          Bukkit.getOnlinePlayers()
            .stream()
            .filter(zombieService::isZombie)
            .forEach(player -> allNightEffects.forEach(player::removePotionEffect));
        });
      }
      
      if (!judgment && daysRemaining == 0) {
        Bukkit.getOnlinePlayers().forEach(player -> titleService.sendTitleWithTypingEffect(player, "judgmentNight", 2));
        Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), configuration.getJudgmentNightSound(), 1.0f, 1.0f));
        judgment = true;
        return;
      }
      
      if (!playersWon && daysRemaining == -1) {
        if (!playerService.getPlayers().isEmpty()) {
          Bukkit.getOnlinePlayers().forEach(player -> titleService.sendTitleWithTypingEffect(player, "playersWonTitle", 2));
          playersWon = true;
        }
        return;
      }
      
      if (!judgment && configuration.isShowRemainingDaysTitle()) {
        Bukkit.getOnlinePlayers().forEach(player -> titleService.sendTitleWithTypingEffect(player, "daysRemaining", 2,
          "%days-remaining%", daysRemaining));
      }
    }
    lastKnownDayTick = currentTime;
  }
  
  private void updateZombieEffects() {
    long currentTime = world.getTime();
    long currentDay = world.getFullTime() / 24000;
    if (!dayFreeze && currentTime > 4000 && currentTime < 4300) {
      Bukkit.getScheduler().runTask(plugin, () -> {
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        dayFreeze = true;
      });
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        dayFreeze = false;
      }, configuration.getFreezeDay());
    }
    
    if (isDayTransition(currentTime)) {
      Set<PotionEffect> effects = configuration.getZombie().getDayEffects().get(currentDay + 1);
      if (effects != null) {
        applyEffects(effects, Collections.emptySet());
      }
    } else if (isNightTransition(currentTime)) {
      Set<PotionEffect> effects = configuration.getZombie().getNightEffects().get(currentDay + 1);
      if (effects != null) {
        applyEffects(effects, Collections.emptySet());
      }
    }
    checkDayCycleAndNotify(currentTime);
  }
  
}
