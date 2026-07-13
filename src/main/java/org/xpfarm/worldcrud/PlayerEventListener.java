package org.xpfarm.worldcrud;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Handles player events for sticky logout functionality
 */
public class PlayerEventListener implements Listener {
    
    private final WorldCRUDPlugin plugin;
    private final PlayerDataManager playerDataManager;
    
    public PlayerEventListener(WorldCRUDPlugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
    }
    
    /**
     * Handle player join events for sticky logout
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (!playerDataManager.isStickyLogoutEnabled()) {
            plugin.logDebug("Sticky logout disabled, skipping teleport for " + player.getName());
            return;
        }
        
        // Delay teleportation slightly to ensure player is fully loaded
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    boolean teleported = playerDataManager.teleportToLastWorld(player);
                    
                    if (teleported) {
                        plugin.logDebug("Successfully teleported " + player.getName() + " to last world on join");
                    } else {
                        plugin.logDebug("No teleportation needed for " + player.getName() + " on join");
                        // Record current world as last world if none exists
                        if (playerDataManager.getLastWorld(player) == null) {
                            playerDataManager.setLastWorld(player, player.getWorld());
                            plugin.logDebug("Recorded initial world for " + player.getName() + ": " + player.getWorld().getName());
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Error handling sticky logout for " + player.getName() + ": " + e.getMessage());
                }
            }
        }.runTaskLater(plugin, 20L); // Delay by 1 second (20 ticks)
    }
    
    /**
     * Handle player quit events to record their last world
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (!playerDataManager.isStickyLogoutEnabled()) {
            return;
        }
        
        try {
            // Record the world they're leaving as their last world
            String currentWorldName = player.getWorld().getName();
            
            if (playerDataManager.isStickyLogoutEnabled(currentWorldName)) {
                playerDataManager.setLastWorld(player, currentWorldName);
                plugin.logDebug("Recorded logout world for " + player.getName() + ": " + currentWorldName);
            } else {
                plugin.logDebug("Sticky logout disabled for world " + currentWorldName + ", not recording for " + player.getName());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error recording last world for " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Handle world change events to track player movement
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        
        if (!playerDataManager.isStickyLogoutEnabled()) {
            return;
        }
        
        try {
            // Record the new world as their current world
            String newWorldName = player.getWorld().getName();
            
            if (playerDataManager.isStickyLogoutEnabled(newWorldName)) {
                playerDataManager.setLastWorld(player, newWorldName);
                plugin.logDebug("Updated last world for " + player.getName() + " to: " + newWorldName);
            } else {
                plugin.logDebug("Sticky logout disabled for world " + newWorldName + ", not updating for " + player.getName());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error updating last world for " + player.getName() + ": " + e.getMessage());
        }
    }
}
