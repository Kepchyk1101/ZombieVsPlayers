package dev.kepchyk1101.zvp.configuration;

import eu.okaeri.configs.OkaeriConfig;
import lombok.*;
import lombok.experimental.FieldDefaults;
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
  
  Database database = new Database("jdbc:sqlite:plugins/ZombieVsPlayers/data.db", "username", "password");
  
  Zombie zombie = new Zombie(
    new SunFire(20L, 100),
    new Golem(60L, 15),
    Set.of(
      new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 1),
      new PotionEffect(PotionEffectType.WATER_BREATHING, Integer.MAX_VALUE, 1)
    ),
    Set.of(
      new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1)
    ),
    Set.of(
      new PotionEffect(PotionEffectType.LUCK, Integer.MAX_VALUE, 1)
    )
  );
  
  boolean showRemainingDaysTitle = true;
  
  Map<String, String> titles = new HashMap<>() {{
    put("playersWonTitle", "&aПобедили игроки!");
    put("zombiesWonTitle", "&aПобедили зомби!");
    put("daysRemaining", "&aОсталось %days-remaining% дней!");
  }};
  
  public void saveAsync() {
    CompletableFuture.runAsync(this::save);
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
    
    Set<PotionEffect> effects;
    
    Set<PotionEffect> dayEffects;
    
    Set<PotionEffect> nightEffects;
    
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
