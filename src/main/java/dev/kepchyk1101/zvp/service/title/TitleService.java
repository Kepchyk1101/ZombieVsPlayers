package dev.kepchyk1101.zvp.service.title;

import dev.kepchyk1101.zvp.service.Service;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public interface TitleService extends Service {
  
  void sendRawTitleWithTypingEffect(@NotNull CommandSender recipient, @NotNull String title, int delay);
  
  void sendTitleWithTypingEffect(@NotNull CommandSender recipient, @NotNull String title, int delay, Object @NotNull ... replacers);
  
}
