package dev.kepchyk1101.zvp.configuration;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PluginConfiguration extends OkaeriConfig {
  
  boolean started = false;
  int day = 1;
  
  Database database = new Database("jdbc:sqlite:plugins/ZombieVsPlayers/data.db", "username", "password");
  
  long dayStartsFrom = 5500L;
  long dayStartsTo = 6500L;
  
  long nightStartsFrom = 11500L;
  long nightStartsTo = 12500L;
  
  long tickerDelay = 60L;
  
  @Comment("Насколько \"продлить\" день в тиках")
  long freezeDay = 5 * 60 * 20L;
  
  Zombie zombie = new Zombie(
    new SunFire(20L, 100),
    new Golem(60L, 15),
    Map.of(
      1L, Set.of(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 1)),
      2L, Set.of(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 2)),
      3L, Set.of(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2))
    ),
    Map.of(
      1L, Set.of(new PotionEffect(PotionEffectType.BAD_OMEN, Integer.MAX_VALUE, 1)),
      2L, Set.of(new PotionEffect(PotionEffectType.ABSORPTION, Integer.MAX_VALUE, 1)),
      3L, Set.of(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 2)),
      4L, Set.of(new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, 2))
    ),
    Map.of(
      Material.ROTTEN_FLESH, Set.of(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1))
    )
  );
  
  boolean showRemainingDaysTitle = true;
  
  Sound judgmentNightSound = Sound.ENTITY_ENDER_DRAGON_DEATH;
  
  Map<String, String> titles = new HashMap<>() {{
    put("playersWonTitle", "&aПобедили игроки!");
    put("zombiesWonTitle", "&aПобедили зомби!");
    put("daysRemaining", "&aОсталось %days-remaining% дней!");
    put("judgmentNight", "&4&lСУДНАЯ НОЧЬ");
    put("gameStarted", "&aВЫЖИВАНИЕ НАЧАЛОСЬ");
  }};
  
  @SuppressWarnings("UnusedReturnValue")
  public CompletableFuture<OkaeriConfig> saveAsync() {
    return CompletableFuture.supplyAsync(this::save);
  }
  
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class Database extends OkaeriConfig {
    
    String url;
    
    String username;
    
    String password;
    
  }
  
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class Zombie extends OkaeriConfig {
    
    SunFire sunFire;
    
    Golem golem;
    
    Map<Long, Set<PotionEffect>> dayEffects;
    
    Map<Long, Set<PotionEffect>> nightEffects;
    
    Map<Material, Set<PotionEffect>> foodEffects;
    
  }
  
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class SunFire extends OkaeriConfig {
    
    long updateDelayTicks;
    
    int fireTicks;
    
  }
  
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class Golem extends OkaeriConfig {
    
    long updateDelayTicks;
    
    int radius;
    
  }
  
}
