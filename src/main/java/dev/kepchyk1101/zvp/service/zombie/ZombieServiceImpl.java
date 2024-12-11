package dev.kepchyk1101.zvp.service.zombie;

import dev.kepchyk1101.zvp.configuration.PluginConfiguration;
import dev.kepchyk1101.zvp.entity.ZombieEntity;
import dev.kepchyk1101.zvp.repository.impl.ZombieRepository;
import dev.kepchyk1101.zvp.service.location.LocationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ZombieServiceImpl implements ZombieService, Listener {
  
  @NotNull LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();
  
  @SuppressWarnings("DataFlowIssue")
  @NotNull World world = Bukkit.getWorld("world");
  
  @NotNull Plugin plugin;
  
  @NotNull ZombieRepository repository;
  
  @NotNull LocationService locationService;
  
  @NotNull PluginConfiguration.Zombie configuration;
  
  @NotNull PluginConfiguration pluginConfiguration;
  
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
    Bukkit.getScheduler().runTaskTimer(plugin, () -> world.getEntitiesByClass(IronGolem.class).forEach(this::golem), 0L, configuration.getGolem().getUpdateDelayTicks());
  }
  
  @Override
  public void startGivingFinCompass() {
    Bukkit.getScheduler().runTaskTimer(plugin, this::updateFindCompasses, 0L, pluginConfiguration.getFindCompass().getUpdateDelayTicks());
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
    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "skin set zombie " + player.getName());
    zombies.add(player.getUniqueId());
    world.getEntitiesByClass(Mob.class)
      .stream()
      .filter(mob -> mob.getTarget() == player)
      .forEach(mob -> mob.setTarget(null));
    
  }
  
  @Override
  public void quit(@NotNull Player player) {
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
    
    Set<PotionEffect> effects = configuration.getFoodEffects().get(event.getItem().getType());
    if (effects == null) {
      return;
    }
    
    player.addPotionEffects(effects);
  }
  
  @EventHandler
  private void on(@NotNull EntityTargetEvent event) {
    if (!(event.getTarget() instanceof Player player)) {
      return;
    }
    
    if (!isZombie(player)) {
      return;
    }
    
    event.setCancelled(true);
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
  
  public void golem(@NotNull IronGolem ironGolem) {
    locationService.findNearestPlayerInRadius(
        ironGolem.getLocation(),
        configuration.getGolem().getRadius()
      )
      .ifPresent(ironGolem::setTarget);
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
    
    if (pluginConfiguration.getDay() >= 6) {
      return false;
    }
    
    if (!isDayTransition(world.getTime())) {
      return false;
    }
    
    if (world.hasStorm() || world.isThundering()) {
      return false;
    }
    
    if (!locationService.isUnderOpenSky(player)) {
      return false;
    }
    
    return player.getLocation().getBlock().getType() != Material.WATER;
  }
  
  private boolean isDayTransition(long currentTime) {
    return currentTime > pluginConfiguration.getDayStartsFrom()
      && currentTime < pluginConfiguration.getDayStartsTo();
  }
  
  private void updateFindCompasses() {
    Bukkit.getOnlinePlayers()
      .stream()
      .filter(this::isZombie)
      .map(HumanEntity::getInventory)
      .filter(inventory -> !inventory.contains(pluginConfiguration.getFindCompass().getItemStack()))
      .forEach(inventory -> inventory.addItem(pluginConfiguration.getFindCompass().getItemStack()));
    
    Bukkit.getOnlinePlayers()
      .stream()
      .filter(this::isZombie)
      .forEach(player -> {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (!itemInHand.isSimilar(pluginConfiguration.getFindCompass().getItemStack())) {
          return;
        }
        
        Player nearestPlayer = getNearestPlayer(player);
        if (nearestPlayer == null) {
          return;
        }
        
        player.setCompassTarget(nearestPlayer.getLocation());
        player.sendActionBar(serializer.deserialize(pluginConfiguration.getFindCompass().getActionBar().replace("%player%", nearestPlayer.getName()).replace("%distance%", String.valueOf((int) nearestPlayer.getLocation().distance(player.getLocation())))));
      });
  }
  
  public @Nullable Player getNearestPlayer(Player player) {
    World world = player.getWorld();
    Location location = player.getLocation();
    ArrayList<Player> playersInWorld = new ArrayList<>(world.getEntitiesByClass(Player.class));
    List<Player> test = new ArrayList<>();
    for (Player player1 : playersInWorld) {
      if (!isZombie(player1)) {
        test.add(player1);
      }
    }
    test.remove(player);
    if (test.isEmpty()) {
      return null;
    }
    test.sort(Comparator.comparingDouble(o -> o.getLocation().distanceSquared(location)));
    return test.get(0);
  }
  
}
