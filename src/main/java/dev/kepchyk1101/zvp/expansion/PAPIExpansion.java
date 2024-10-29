package dev.kepchyk1101.zvp.expansion;

import dev.kepchyk1101.zvp.service.game.GameService;
import dev.kepchyk1101.zvp.service.game.GameTeam;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PAPIExpansion extends PlaceholderExpansion {
  
  @NotNull GameService gameService;
  
  @NotNull Map<GameTeam, String> xzKakNazvat = Map.of(
    GameTeam.ZOMBIE, "Zombie",
    GameTeam.PLAYER, "Player"
  );
  
  @Override
  public @NotNull String getIdentifier() {
    return "zvp";
  }
  
  @Override
  public @NotNull String getAuthor() {
    return "Kepchyk1101";
  }
  
  @Override
  public @NotNull String getVersion() {
    return "1.0.0";
  }
  
  @Override
  public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
    return switch (params) {
      case "team" -> xzKakNazvat.get(gameService.getTeamOf(player));
      default -> null;
    };
  }
}
