package org.xpfarm.worldcrud;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.logging.Logger;

public class WorldCRUDPlugin extends JavaPlugin {
    
    private static WorldCRUDPlugin instance;
    private Logger logger;
    private FileConfiguration config;
    private WorldManager worldManager;
    private PermissionHandler permissionHandler;
    private PlayerDataManager playerDataManager;
    private PlayerEventListener playerEventListener;
    private boolean debugMode = false;
    
    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        
        // Save default config
        saveDefaultConfig();
        config = getConfig();
        debugMode = config.getBoolean("debug-mode", false);
        
        // Initialize components
        worldManager = new WorldManager(this);
        permissionHandler = new PermissionHandler(this);
        playerDataManager = new PlayerDataManager(this);
        
        // Register event listener
        playerEventListener = new PlayerEventListener(this, playerDataManager);
        getServer().getPluginManager().registerEvents(playerEventListener, this);
        
        // Register command
        WorldCRUDCommand commandExecutor = new WorldCRUDCommand(this);
        getCommand("worldcrud").setExecutor(commandExecutor);
        getCommand("worldcrud").setTabCompleter(commandExecutor);
        
        logger.info("WorldCRUD plugin enabled successfully!");
        logDebug("Debug mode is enabled");
    }
    
    @Override
    public void onDisable() {
        if (worldManager != null) {
            worldManager.saveAllWorlds();
        }
        
        if (playerDataManager != null) {
            playerDataManager.savePlayerData();
        }
        
        logger.info("WorldCRUD plugin disabled");
    }
    
    public static WorldCRUDPlugin getInstance() {
        return instance;
    }
    
    public WorldManager getWorldManager() {
        return worldManager;
    }
    
    public PermissionHandler getPermissionHandler() {
        return permissionHandler;
    }
    
    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        config.set("debug-mode", debugMode);
        saveConfig();
    }
    
    public void logDebug(String message) {
        if (debugMode) {
            logger.info("[DEBUG] " + message);
        }
    }
    
    public void sendMessage(CommandSender sender, String message, NamedTextColor color) {
        sender.sendMessage(Component.text(message, color));
    }
    
    public void sendMessage(CommandSender sender, Component message) {
        sender.sendMessage(message);
    }
    
    public void reloadPluginConfig() {
        reloadConfig();
        config = getConfig();
        debugMode = config.getBoolean("debug-mode", false);
        worldManager.reloadConfig();
        logDebug("Configuration reloaded");
    }
}
