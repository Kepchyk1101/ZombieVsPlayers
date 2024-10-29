package dev.kepchyk1101.zvp.service.zombie;

import dev.kepchyk1101.zvp.configuration.PluginConfiguration;
import dev.kepchyk1101.zvp.entity.ZombieEntity;
import dev.kepchyk1101.zvp.repository.impl.ZombieRepository;
import dev.kepchyk1101.zvp.service.location.LocationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.skinsrestorer.api.SkinsRestorerProvider;
import net.skinsrestorer.api.property.SkinIdentifier;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ZombieServiceImpl implements ZombieService, Listener {
  
  @NotNull Plugin plugin;
  
  @NotNull ZombieRepository repository;
  
  @NotNull LocationService locationService;
  
  @NotNull PluginConfiguration.Zombie configuration;
  
  @NotNull Set<UUID> zombies = new HashSet<>();
  
  @Override
  public void enable() {
    zombies.addAll(
      repository.findAll()
        .stream()
        .map(ZombieEntity::getUuid)
        .collect(Collectors.toSet())
    );
    Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::setFireOnZombies, 0L, configuration.getSunFire().getUpdateDelayTicks());
    Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      Bukkit.getWorlds().forEach(world -> {
        world.getEntitiesByClass(IronGolem.class).forEach(this::test);
      });
    }, 0L, 60L);
  }
  
  @Override
  public void disable() {
    repository.deleteAll();
    repository.saveAll(
      zombies.stream()
        .map(ZombieEntity::new)
        .collect(Collectors.toSet())
    );
    zombies.clear();
  }
  
  @Override
  public boolean isZombie(@NotNull Player player) {
    return zombies.contains(player.getUniqueId());
  }
  
  @Override
  public void join(@NotNull Player player) {
    SkinsRestorerProvider.get().getSkinApplier(Player.class).applySkin(player, SkinIdentifier.ofPlayer(UUID.fromString("02b0e86d-c86a-4ae7-bc41-015d21f80c1c")));
    Bukkit.getScheduler().runTaskLater(plugin, () -> player.addPotionEffects(configuration.getEffects()), 5L);
    zombies.add(player.getUniqueId());
  }
  
  @Override
  public void quit(@NotNull Player player) {
    configuration.getEffects()
      .stream()
      .map(PotionEffect::getType)
      .forEach(player::removePotionEffect);
    zombies.remove(player.getUniqueId());
  }
  
  @Override
  public @NotNull Set<@NotNull UUID> getZombies() {
    return Set.copyOf(zombies);
  }
  
  @EventHandler
  private void on(@NotNull PlayerItemConsumeEvent event) {
    Player player = event.getPlayer();
    if (!isZombie(player)) {
      return;
    }
    
    switch (event.getItem().getType()) {
      case ROTTEN_FLESH -> player.setFoodLevel(player.getFoodLevel() + 10);
      case SPIDER_EYE -> player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0));
      case POISONOUS_POTATO -> player.setFoodLevel(player.getFoodLevel() + 5);
    }
  }
  
  @EventHandler
  private void on(@NotNull PlayerInteractEvent event) {
    if (!isZombie(event.getPlayer())) {
      return;
    }
    
    ItemStack item = event.getItem();
    if (item == null || item.getType() != Material.ENDER_PEARL) {
      return;
    }
    
    event.setCancelled(true);
    
  }
  
  @EventHandler
  private void on(@NotNull InventoryOpenEvent event) {
    if (!isZombie((Player) event.getPlayer())) {
      return;
    }
    
    InventoryType inventoryType = event.getInventory().getType();
    if (inventoryType != InventoryType.MERCHANT
      && inventoryType != InventoryType.BREWING) {
      return;
    }
    
    event.setCancelled(true);
  }
  
  @Override
  public void setFireOnZombies() {
    Bukkit.getOnlinePlayers()
      .stream()
      .filter(this::isZombie)
      .filter(this::shouldBurn)
      .forEach(this::setFireOnZombie);
  }
  
  public void test(@NotNull IronGolem ironGolem) {
    locationService.findNearestPlayerInRadius(ironGolem.getLocation(), 15).ifPresent(ironGolem::setTarget);
  }
  
  @Override
  public void setFireOnZombie(@NotNull Player player) {
    player.setFireTicks(configuration.getSunFire().getFireTicks());
  }
  
  private boolean shouldBurn(@NotNull Player player) {
    World world = player.getWorld();
    if (player.getGameMode() != GameMode.SURVIVAL) {
      return false;
    }
    
    if (world.getTime() > 12300 && world.getTime() < 23850) {
      return false;
    }
    
    if (world.hasStorm() || world.isThundering()) {
      return false;
    }
    
    if (!locationService.isUnderOpenSky(player)) {
      return false;
    }
    
    Material blockType = player.getLocation().getBlock().getType();
    return blockType != Material.WATER && blockType != Material.LEGACY_STATIONARY_WATER;
  }
  
}
