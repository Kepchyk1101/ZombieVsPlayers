package dev.kepchyk1101.zvp;

import co.aikar.commands.PaperCommandManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import dev.kepchyk1101.zvp.command.ZombieVsPlayerCommand;
import dev.kepchyk1101.zvp.configuration.PluginConfiguration;
import dev.kepchyk1101.zvp.expansion.PAPIExpansion;
import dev.kepchyk1101.zvp.repository.impl.PlayerRepository;
import dev.kepchyk1101.zvp.repository.impl.ZombieRepository;
import dev.kepchyk1101.zvp.service.game.GameService;
import dev.kepchyk1101.zvp.service.game.GameServiceImpl;
import dev.kepchyk1101.zvp.service.location.LocationService;
import dev.kepchyk1101.zvp.service.location.LocationServiceImpl;
import dev.kepchyk1101.zvp.service.player.PlayerService;
import dev.kepchyk1101.zvp.service.player.PlayerServiceImpl;
import dev.kepchyk1101.zvp.service.title.TitleService;
import dev.kepchyk1101.zvp.service.title.TitleServiceImpl;
import dev.kepchyk1101.zvp.service.zombie.ZombieService;
import dev.kepchyk1101.zvp.service.zombie.ZombieServiceImpl;
import eu.okaeri.configs.ConfigManager;
import eu.okaeri.configs.validator.okaeri.OkaeriValidator;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import eu.okaeri.configs.yaml.bukkit.serdes.SerdesBukkit;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import java.io.File;

@SuppressWarnings("FieldCanBeLocal")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ZombieVsPlayers extends JavaPlugin {
  
  @MonotonicNonNull
  PluginConfiguration configuration;
  
  @MonotonicNonNull
  ConnectionSource connectionSource;
  
  @MonotonicNonNull
  ZombieRepository zombieRepository;
  
  @MonotonicNonNull
  PlayerRepository playerRepository;
  
  @MonotonicNonNull
  TitleService titleService;
  
  @MonotonicNonNull
  LocationService locationService;
  
  @MonotonicNonNull
  PlayerService playerService;
  
  @MonotonicNonNull
  ZombieService zombieService;
  
  @MonotonicNonNull
  GameService gameService;
  
  @MonotonicNonNull
  PAPIExpansion papiExpansion;
  
  @Override
  @SneakyThrows
  public void onEnable() {
    configuration = (PluginConfiguration) ConfigManager.create(PluginConfiguration.class)
      .withConfigurer(new OkaeriValidator(new YamlBukkitConfigurer(), true), new SerdesBukkit())
      .withBindFile(new File(getDataFolder(), "config.yml"))
      .withRemoveOrphans(true)
      .saveDefaults()
      .load(true);
    
    connectionSource = new JdbcPooledConnectionSource(
      configuration.getDatabase().getUrl(),
      configuration.getDatabase().getUsername(),
      configuration.getDatabase().getPassword()
    );
    
    zombieRepository = new ZombieRepository(connectionSource);
    playerRepository = new PlayerRepository(connectionSource);
    
    titleService = new TitleServiceImpl(this, configuration);
    titleService.enable();
    
    locationService = new LocationServiceImpl();
    locationService.enable();
    
    playerService = new PlayerServiceImpl(playerRepository);
    Bukkit.getPluginManager().registerEvents((Listener) playerService, this);
    playerService.enable();
    
    zombieService = new ZombieServiceImpl(this, zombieRepository, locationService, configuration.getZombie());
    Bukkit.getPluginManager().registerEvents((Listener) zombieService, this);
    zombieService.enable();
    
    gameService = new GameServiceImpl(this, zombieService, playerService, titleService, configuration);
    Bukkit.getPluginManager().registerEvents((Listener) gameService, this);
    gameService.enable();
    
    PaperCommandManager commandManager = new PaperCommandManager(this);
    commandManager.registerCommand(new ZombieVsPlayerCommand(gameService, configuration));
    
    papiExpansion = new PAPIExpansion(gameService);
    papiExpansion.register();
  }
  
  @Override
  @SneakyThrows
  public void onDisable() {
    papiExpansion.unregister();
    
    gameService.disable();
    zombieService.disable();
    playerService.disable();
    locationService.disable();
    titleService.disable();
    
    connectionSource.close();
  }
  
}
