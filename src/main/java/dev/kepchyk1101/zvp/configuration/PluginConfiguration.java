package dev.kepchyk1101.zvp.configuration;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import lombok.*;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
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
  long dayStartsFrom = 0L;
  long dayStartsTo = 11999L;
  long nightStartsFrom = 12000L;
  long nightStartsTo = 23999L;
  long tickerDelay = 60L;
  @Comment("Насколько \"продлить\" день в тиках")
  long freezeDay = 5 * 60 * 20L;
  FindCompass findCompass;
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
    put("daysRemaining", "&a%left-declension% %days-remaining% %days-declension%!");
    put("judgmentNight", "&4&lСУДНАЯ НОЧЬ");
    put("gameStarted", "&aВЫЖИВАНИЕ НАЧАЛОСЬ");
  }};
  
  {
    ItemStack itemStack = new ItemStack(Material.COMPASS);
    ItemMeta itemMeta = itemStack.getItemMeta();
    itemMeta.displayName(Component.text("Поисковый компас"));
    itemMeta.lore(List.of(
      Component.text("Держа в руке - указывает на ближайшего игрока")
    ));
    itemStack.addItemFlags(ItemFlag.values());
    itemMeta.addEnchant(Enchantment.LUCK, 1, true);
    itemStack.setItemMeta(itemMeta);
    findCompass = new FindCompass(60L, itemStack, "Ближайший игрок: %player% Дистанция: %distance%");
  }
  
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
  
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  @FieldDefaults(level = AccessLevel.PRIVATE)
  public static class FindCompass extends OkaeriConfig {
    
    long updateDelayTicks;
    
    ItemStack itemStack;
    
    String actionBar;
    
  }
  
}
