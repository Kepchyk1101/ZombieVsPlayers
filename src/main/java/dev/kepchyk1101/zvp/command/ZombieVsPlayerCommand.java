package dev.kepchyk1101.zvp.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import dev.kepchyk1101.zvp.configuration.PluginConfiguration;
import dev.kepchyk1101.zvp.service.game.GameService;
import dev.kepchyk1101.zvp.service.game.GameTeam;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@CommandAlias("zvp")
@RequiredArgsConstructor
@CommandPermission("zvp.admin")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ZombieVsPlayerCommand extends BaseCommand {
  
  @NotNull GameService gameService;
  
  @NotNull PluginConfiguration configuration;
  
  @Subcommand("test")
  public void test(@NotNull Player player) {
    player.sendMessage(Component.text(PlaceholderAPI.setPlaceholders(player, "%zvp_team%")));
  }
  
  @Subcommand("swap")
  public void swap(@NotNull CommandSender sender, @NotNull OnlinePlayer player, @NotNull GameTeam team) {
    gameService.swapTeam(player.getPlayer(), team);
  }
  
  @Subcommand("showremainingdays")
  public void showRemainingDays(@NotNull Player player, boolean show) {
    configuration.setShowRemainingDaysTitle(show);
    player.sendMessage(Component.text("Успешно").color(NamedTextColor.GREEN));
  }
  
}
