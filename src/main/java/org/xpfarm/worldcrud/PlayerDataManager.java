package org.xpfarm.worldcrud;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player data for sticky logout functionality
 */
public class PlayerDataManager {
    
    private final WorldCRUDPlugin plugin;
    private final File playerDataFile;
    private FileConfiguration playerDataConfig;
    private final Map<UUID, String> lastWorldCache;
    
    public PlayerDataManager(WorldCRUDPlugin plugin) {
        this.plugin = plugin;
        this.playerDataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        this.lastWorldCache = new ConcurrentHashMap<>();
        loadPlayerData();
    }
    
    /**
     * Load player data from file
     */
    private void loadPlayerData() {
        if (!playerDataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerdata.yml: " + e.getMessage());
                return;
            }
        }
        
        playerDataConfig = YamlConfiguration.loadConfiguration(playerDataFile);
        
        // Load cached data
        if (playerDataConfig.contains("last-worlds")) {
            for (String uuidString : playerDataConfig.getConfigurationSection("last-worlds").getKeys(false)) {
                try {
                    UUID playerUuid = UUID.fromString(uuidString);
                    String worldName = playerDataConfig.getString("last-worlds." + uuidString);
                    if (worldName != null) {
                        lastWorldCache.put(playerUuid, worldName);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.logDebug("Invalid UUID in playerdata.yml: " + uuidString);
                }
            }
        }
        
        plugin.logDebug("Loaded player data for " + lastWorldCache.size() + " players");
    }
    
    /**
     * Save player data to file
     */
    public void savePlayerData() {
        try {
            // Clear existing data
            playerDataConfig.set("last-worlds", null);
            
            // Save cached data
            for (Map.Entry<UUID, String> entry : lastWorldCache.entrySet()) {
                playerDataConfig.set("last-worlds." + entry.getKey().toString(), entry.getValue());
            }
            
            playerDataConfig.save(playerDataFile);
            plugin.logDebug("Saved player data for " + lastWorldCache.size() + " players");
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerdata.yml: " + e.getMessage());
        }
    }
    
    /**
     * Record a player's current world as their last world
     */
    public void setLastWorld(Player player, String worldName) {
        if (player == null || worldName == null) return;
        
        lastWorldCache.put(player.getUniqueId(), worldName);
        plugin.logDebug("Set last world for " + player.getName() + ": " + worldName);
    }
    
    /**
     * Record a player's current world as their last world
     */
    public void setLastWorld(Player player, World world) {
        if (world != null) {
            setLastWorld(player, world.getName());
        }
    }
    
    /**
     * Get a player's last world
     */
    public String getLastWorld(Player player) {
        if (player == null) return null;
        return lastWorldCache.get(player.getUniqueId());
    }
    
    /**
     * Get a player's last world by UUID
     */
    public String getLastWorld(UUID playerUuid) {
        return lastWorldCache.get(playerUuid);
    }
    
    /**
     * Check if sticky logout is enabled for a specific world
     */
    public boolean isStickyLogoutEnabled(String worldName) {
        if (worldName == null) return false;
        
        FileConfiguration config = plugin.getConfig();
        
        // Check global setting first
        boolean globalEnabled = config.getBoolean("sticky-logout.enabled", true);
        if (!globalEnabled) return false;
        
        // Check world-specific setting
        String worldPath = "sticky-logout.worlds." + worldName + ".enabled";
        if (config.contains(worldPath)) {
            return config.getBoolean(worldPath, true);
        }
        
        // Default to enabled if not specified
        return true;
    }
    
    /**
     * Check if sticky logout is enabled globally
     */
    public boolean isStickyLogoutEnabled() {
        return plugin.getConfig().getBoolean("sticky-logout.enabled", true);
    }
    
    /**
     * Teleport player to their last world if sticky logout is enabled
     * @param player The player to teleport
     * @return true if player was teleported, false otherwise
     */
    public boolean teleportToLastWorld(Player player) {
        if (!isStickyLogoutEnabled()) {
            plugin.logDebug("Sticky logout is globally disabled");
            return false;
        }
        
        String lastWorldName = getLastWorld(player);
        if (lastWorldName == null) {
            plugin.logDebug("No last world recorded for " + player.getName());
            return false;
        }
        
        if (!isStickyLogoutEnabled(lastWorldName)) {
            plugin.logDebug("Sticky logout disabled for world: " + lastWorldName);
            return false;
        }
        
        World lastWorld = Bukkit.getWorld(lastWorldName);
        if (lastWorld == null) {
            plugin.logDebug("Last world no longer exists: " + lastWorldName);
            // Clean up invalid world reference
            lastWorldCache.remove(player.getUniqueId());
            return false;
        }
        
        // Don't teleport if already in the correct world
        if (player.getWorld().equals(lastWorld)) {
            plugin.logDebug("Player " + player.getName() + " already in last world: " + lastWorldName);
            return false;
        }
        
        // Use WorldManager for teleportation if available
        if (plugin.getWorldManager().teleportToWorld(player, lastWorldName)) {
            plugin.sendMessage(player, "Welcome back! Returned to your last world: " + lastWorldName, 
                             net.kyori.adventure.text.format.NamedTextColor.GREEN);
            plugin.logDebug("Teleported " + player.getName() + " to last world: " + lastWorldName);
            return true;
        } else {
            plugin.getLogger().warning("Failed to teleport " + player.getName() + " to last world: " + lastWorldName);
            return false;
        }
    }
    
    /**
     * Remove a player's data (for cleanup)
     */
    public void removePlayerData(UUID playerUuid) {
        lastWorldCache.remove(playerUuid);
    }
    
    /**
     * Get the number of tracked players
     */
    public int getTrackedPlayerCount() {
        return lastWorldCache.size();
    }
    
    /**
     * Clear all player data
     */
    public void clearAllData() {
        lastWorldCache.clear();
        savePlayerData();
    }
}
