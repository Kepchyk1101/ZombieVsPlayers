package dev.kepchyk1101.zvp.service.game;

import dev.kepchyk1101.zvp.configuration.PluginConfiguration;
import dev.kepchyk1101.zvp.exception.GameAlreadyStarted;
import dev.kepchyk1101.zvp.service.player.PlayerService;
import dev.kepchyk1101.zvp.service.title.TitleService;
import dev.kepchyk1101.zvp.service.zombie.ZombieService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
import org.bukkit.event.world.TimeSkipEvent;
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
  boolean dayFreeze = false;
  
  @NonFinal
  boolean judgment = false;
  
  @NonFinal
  boolean playersWon = false;
  
  @NonFinal
  long lastKnownDayTick;
  
  @Getter
  @Setter
  @NonFinal
  boolean started = false;
  
  @Getter
  @Setter
  @NonFinal
  long currentDay = 1L;
  
  @Override
  public void enable() {
    Bukkit.getOnlinePlayers().forEach(this::join);
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
    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::tick, 0L, configuration.getTickerDelay());
    configuration.setStarted(true);
    configuration.setDay((int) currentDay);
    configuration.saveAsync();
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
      updateZombieEffects(world.getTime());
    }
    if (playerService.getPlayers().isEmpty()) {
      titleService.broadcastTitle("zombiesWonTitle", 2);
    }
  }
  
  @EventHandler
  private void on(@NotNull TimeSkipEvent event) {
    if (event.getSkipReason() == TimeSkipEvent.SkipReason.NIGHT_SKIP && started) {
      event.setCancelled(true);
    }
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
      currentDay++;
      configuration.setDay((int) currentDay);
      configuration.saveAsync();
      long daysRemaining = 7 - currentDay;
      
      if (currentDay >= 6) {
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
      
      if (!playersWon && currentDay == 8) {
        if (!playerService.getPlayers().isEmpty()) {
          titleService.broadcastTitle("playersWonTitle", 2);
          playersWon = true;
        }
        lastKnownDayTick = currentTime;
        return;
      }
      
      if (!judgment && currentDay == 7) {
        titleService.broadcastTitle("judgmentNight", 2);
        Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), configuration.getJudgmentNightSound(), 1.0f, 1.0f));
        judgment = true;
        lastKnownDayTick = currentTime;
        return;
      }
      
      if (!judgment && configuration.isShowRemainingDaysTitle()) {
        titleService.broadcastTitle("daysRemaining", 2,
          "%days-remaining%", daysRemaining);
      }
    }
    lastKnownDayTick = currentTime;
  }
  
  @Override
  public void tick() {
    long currentDayTimeTicks = world.getTime();
    if (!dayFreeze && currentDayTimeTicks > 4000 && currentDayTimeTicks < 4300) {
      Bukkit.getScheduler().runTask(plugin, () -> {
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        dayFreeze = true;
      });
      Bukkit.getScheduler().runTaskLater(plugin, () -> {
        world.setTime(4301);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, true);
        dayFreeze = false;
      }, configuration.getFreezeDay());
    }
    
    updateZombieEffects(currentDayTimeTicks);
    checkDayCycleAndNotify(currentDayTimeTicks);
  }
  
  private void updateZombieEffects(long currentDayTimeTicks) {
    if (isDayTransition(currentDayTimeTicks)) {
      Set<PotionEffect> effects = configuration.getZombie().getDayEffects().get(currentDay);
      if (effects != null) {
        applyEffects(effects, Collections.emptySet());
      }
    } else if (isNightTransition(currentDayTimeTicks)) {
      Set<PotionEffect> effects = configuration.getZombie().getNightEffects().get(currentDay);
      if (effects != null) {
        applyEffects(effects, Collections.emptySet());
      }
    }
  }
  
  private boolean isDayTransition(long currentTime) {
    return currentTime > configuration.getDayStartsFrom()
      && currentTime < configuration.getDayStartsTo();
  }
  
  private boolean isNightTransition(long currentTime) {
    return currentTime > configuration.getNightStartsFrom()
      && currentTime < configuration.getNightStartsTo();
  }
  
}
