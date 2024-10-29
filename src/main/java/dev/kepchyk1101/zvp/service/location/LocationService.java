package dev.kepchyk1101.zvp.service.location;

import dev.kepchyk1101.zvp.service.Service;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface LocationService extends Service {
  
  boolean isUnderOpenSky(@NotNull Entity entity);
  
  Optional<@NotNull Player> findNearestPlayerInRadius(@NotNull Location location, double radius);
  
}
