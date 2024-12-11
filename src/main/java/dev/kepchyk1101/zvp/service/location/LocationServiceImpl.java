package dev.kepchyk1101.zvp.service.location;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class LocationServiceImpl implements LocationService {
  
  @Override
  public boolean isUnderOpenSky(@NotNull Entity entity) {
    Location entityLocation = entity.getLocation();
    return entityLocation.getY() >= entity.getWorld().getHighestBlockYAt(entityLocation);
  }
  
  @Override
  public Optional<@NotNull Player> findNearestPlayerInRadius(@NotNull Location location, double radius) {
    Player nearestPlayer = null;
    double nearestDistance = radius;
    for (Player player : location.getWorld().getPlayers()) {
      double distance = player.getLocation().distance(location);
      
      if (distance <= radius && (nearestPlayer == null || distance < nearestDistance)) {
        nearestPlayer = player;
        nearestDistance = distance;
      }
    }
    
    return Optional.ofNullable(nearestPlayer);
  }
  
}
