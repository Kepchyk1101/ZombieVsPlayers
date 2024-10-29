package dev.kepchyk1101.zvp.service.title;

import dev.kepchyk1101.zvp.configuration.PluginConfiguration;
import dev.kepchyk1101.zvp.utility.TextUtility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TitleServiceImpl implements TitleService {
  
  @NotNull Plugin plugin;
  
  @NotNull PluginConfiguration configuration;
  
  @Override
  public void sendRawTitleWithTypingEffect(@NotNull CommandSender recipient, @NotNull String title, int delay) {
    String fullText = LegacyComponentSerializer.legacySection().serialize(LegacyComponentSerializer.legacyAmpersand().deserialize(title));
    new BukkitRunnable() {
      int index = 0;
      
      @Override
      public void run() {
        if (index <= fullText.length()) {
          Component partialMessage = LegacyComponentSerializer.legacySection().deserialize(fullText.substring(0, index));
          
          recipient.showTitle(
            Title.title(
              partialMessage,
              Component.empty(),
              Title.Times.of(
                Duration.ZERO,
                Duration.ofSeconds(10),
                Duration.ZERO
              )
            )
          );
          
          index++;
        } else {
          this.cancel();
        }
      }
    }.runTaskTimer(plugin, 0L, delay);
  }
  
  @Override
  public void sendTitleWithTypingEffect(@NotNull CommandSender recipient, @NotNull String title, int delay, Object @NotNull ... replacers) {
    sendRawTitleWithTypingEffect(recipient, TextUtility.applyReplacers(configuration.getTitles().get(title), replacers), delay);
  }
  
}
