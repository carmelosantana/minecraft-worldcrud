package org.xpfarm.worldcrud;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.xpfarm.worldcrud.generators.ClassicSkyblockGenerator;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

public class WorldManager {
    
    private final WorldCRUDPlugin plugin;
    private final Logger logger;
    private final Set<String> managedWorlds;
    private final Map<String, WorldTypes> worldTypeMap;
    
    public WorldManager(WorldCRUDPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        this.managedWorlds = new HashSet<>();
        this.worldTypeMap = new HashMap<>();
        
        // Load existing worlds
        loadExistingWorlds();
    }
    
    public boolean createWorld(String worldName, WorldTypes worldType) {
        return createWorld(worldName, worldType, -1);
    }
    
    public boolean createWorld(String worldName, WorldTypes worldType, int borderSize) {
        if (worldName == null || worldName.trim().isEmpty()) {
            return false;
        }
        
        worldName = worldName.trim();
        
        // Check if world already exists
        if (Bukkit.getWorld(worldName) != null) {
            plugin.logDebug("World " + worldName + " already exists");
            return false;
        }
        
        try {
            WorldCreator creator = new WorldCreator(worldName);
            
            // Set world type based on enum
            switch (worldType) {
                case NORMAL:
                    creator.environment(World.Environment.NORMAL);
                    break;
                case SKYBLOCK_CLASSIC:
                    creator.environment(World.Environment.NORMAL);
                    creator.generator(new ClassicSkyblockGenerator());
                    creator.generateStructures(false);
                    plugin.logDebug("Creating Classic Skyblock world with custom generator");
                    break;
            }
            
            World world = creator.createWorld();
            if (world != null) {
                // For skyblock worlds, we need to populate the spawn chunk with chests and other features
                if (worldType == WorldTypes.SKYBLOCK_CLASSIC) {
                    // Schedule population for next tick to ensure chunk is fully generated
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        populateSkyblockWorld(world, worldType);
                    });
                }
                
                // Set world border if size is specified
                if (borderSize > 0) {
                    WorldBorder border = world.getWorldBorder();
                    border.setCenter(world.getSpawnLocation());
                    border.setSize(borderSize * 2); // borderSize is radius, setSize expects diameter
                    plugin.logDebug("Set world border for " + worldName + " to " + borderSize + " blocks radius");
                }
                
                managedWorlds.add(worldName);
                worldTypeMap.put(worldName, worldType);
                plugin.logDebug("Created world: " + worldName + " of type: " + worldType + 
                    (borderSize > 0 ? " with border size: " + borderSize : ""));
                return true;
            }
        } catch (Exception e) {
            logger.severe("Failed to create world " + worldName + ": " + e.getMessage());
        }
        
        return false;
    }
    
    public boolean deleteWorld(String worldName) {
        if (worldName == null || worldName.trim().isEmpty()) {
            return false;
        }
        
        worldName = worldName.trim();
        World world = Bukkit.getWorld(worldName);
        
        if (world == null) {
            plugin.logDebug("World " + worldName + " does not exist");
            return false;
        }
        
        // Teleport all players out of the world
        Location spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
        for (Player player : world.getPlayers()) {
            player.teleport(spawnLocation);
            plugin.sendMessage(player, "You have been teleported out of " + worldName + " as it's being deleted", NamedTextColor.YELLOW);
        }
        
        // Unload the world
        if (Bukkit.unloadWorld(world, false)) {
            // Delete world files
            File worldFolder = world.getWorldFolder();
            if (deleteWorldFolder(worldFolder)) {
                managedWorlds.remove(worldName);
                worldTypeMap.remove(worldName);
                plugin.logDebug("Deleted world: " + worldName);
                return true;
            }
        }
        
        return false;
    }
    
    public boolean renameWorld(String oldName, String newName) {
        if (oldName == null || newName == null || oldName.trim().isEmpty() || newName.trim().isEmpty()) {
            return false;
        }
        
        oldName = oldName.trim();
        newName = newName.trim();
        
        World world = Bukkit.getWorld(oldName);
        if (world == null) {
            return false;
        }
        
        // Check if new name already exists
        if (Bukkit.getWorld(newName) != null) {
            return false;
        }
        
        // Teleport players out
        Location spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
        for (Player player : world.getPlayers()) {
            player.teleport(spawnLocation);
        }
        
        // Unload and rename
        if (Bukkit.unloadWorld(world, true)) {
            File oldFolder = world.getWorldFolder();
            File newFolder = new File(oldFolder.getParent(), newName);
            
            if (oldFolder.renameTo(newFolder)) {
                managedWorlds.remove(oldName);
                managedWorlds.add(newName);
                WorldTypes worldType = worldTypeMap.remove(oldName);
                if (worldType != null) {
                    worldTypeMap.put(newName, worldType);
                }
                plugin.logDebug("Renamed world from " + oldName + " to " + newName);
                return true;
            }
        }
        
        return false;
    }
    
    public boolean teleportToWorld(Player player, String worldName) {
        if (worldName == null || worldName.trim().isEmpty()) {
            return false;
        }
        
        worldName = worldName.trim();
        World world = Bukkit.getWorld(worldName);
        
        if (world == null) {
            plugin.logDebug("World " + worldName + " does not exist for teleportation");
            return false;
        }
        
        Location spawnLocation = world.getSpawnLocation();
        player.teleport(spawnLocation);
        plugin.logDebug("Teleported " + player.getName() + " to world " + worldName);
        return true;
    }
    
    public boolean setWorldSpawn(String worldName, Location location) {
        if (worldName == null || worldName.trim().isEmpty() || location == null) {
            return false;
        }
        
        worldName = worldName.trim();
        World world = Bukkit.getWorld(worldName);
        
        if (world == null) {
            return false;
        }
        
        world.setSpawnLocation(location);
        plugin.logDebug("Set spawn for world " + worldName + " to " + location);
        return true;
    }
    
    public boolean resetWorld(String worldName) {
        if (worldName == null || worldName.trim().isEmpty()) {
            return false;
        }
        
        worldName = worldName.trim();
        WorldTypes worldType = worldTypeMap.get(worldName);
        
        if (worldType == null) {
            worldType = WorldTypes.NORMAL;
        }
        
        // Delete and recreate
        if (deleteWorld(worldName)) {
            return createWorld(worldName, worldType);
        }
        
        return false;
    }
    
    public boolean clearWorld(String worldName) {
        if (worldName == null || worldName.trim().isEmpty()) {
            return false;
        }
        
        worldName = worldName.trim();
        World world = Bukkit.getWorld(worldName);
        
        if (world == null) {
            return false;
        }
        
        // Remove all entities except players
        for (Entity entity : world.getEntities()) {
            if (!(entity instanceof Player)) {
                entity.remove();
            }
        }
        
        // Remove all items
        for (Item item : world.getEntitiesByClass(Item.class)) {
            item.remove();
        }
        
        plugin.logDebug("Cleared entities and items from world " + worldName);
        return true;
    }
    
    public boolean saveWorld(String worldName) {
        if (worldName == null || worldName.trim().isEmpty()) {
            return false;
        }
        
        worldName = worldName.trim();
        World world = Bukkit.getWorld(worldName);
        
        if (world == null) {
            return false;
        }
        
        world.save();
        plugin.logDebug("Saved world " + worldName);
        return true;
    }
    
    public boolean setWorldBorder(String worldName, int borderSize) {
        if (worldName == null || worldName.trim().isEmpty() || borderSize <= 0) {
            return false;
        }
        
        worldName = worldName.trim();
        World world = Bukkit.getWorld(worldName);
        
        if (world == null) {
            return false;
        }
        
        WorldBorder border = world.getWorldBorder();
        border.setCenter(world.getSpawnLocation());
        border.setSize(borderSize * 2); // borderSize is radius, setSize expects diameter
        
        plugin.logDebug("Set world border for " + worldName + " to " + borderSize + " blocks radius");
        return true;
    }
    
    public int getWorldBorderRadius(String worldName) {
        if (worldName == null || worldName.trim().isEmpty()) {
            return -1;
        }
        
        worldName = worldName.trim();
        World world = Bukkit.getWorld(worldName);
        
        if (world == null) {
            return -1;
        }
        
        WorldBorder border = world.getWorldBorder();
        return (int) (border.getSize() / 2); // Convert diameter back to radius
    }

    public boolean loadWorld(String worldName) {
        if (worldName == null || worldName.trim().isEmpty()) {
            return false;
        }
        
        worldName = worldName.trim();
        
        // Check if world already loaded
        if (Bukkit.getWorld(worldName) != null) {
            return true;
        }
        
        // Try to load existing world
        File worldFolder = new File(Bukkit.getWorldContainer(), worldName);
        if (worldFolder.exists() && worldFolder.isDirectory()) {
            WorldCreator creator = new WorldCreator(worldName);
            World world = creator.createWorld();
            
            if (world != null) {
                managedWorlds.add(worldName);
                plugin.logDebug("Loaded world " + worldName);
                return true;
            }
        }
        
        return false;
    }
    
    public List<String> getWorldList() {
        List<String> worlds = new ArrayList<>();
        for (World world : Bukkit.getWorlds()) {
            worlds.add(world.getName());
        }
        return worlds;
    }
    
    public WorldInfo getWorldInfo(String worldName) {
        if (worldName == null || worldName.trim().isEmpty()) {
            return null;
        }
        
        worldName = worldName.trim();
        World world = Bukkit.getWorld(worldName);
        
        if (world == null) {
            return null;
        }
        
        return new WorldInfo(world, worldTypeMap.get(worldName));
    }
    
    public void saveAllWorlds() {
        for (World world : Bukkit.getWorlds()) {
            world.save();
        }
        plugin.logDebug("Saved all worlds");
    }
    
    public void reloadConfig() {
        // Reload any world-specific configuration
        plugin.logDebug("WorldManager configuration reloaded");
    }
    
    private void loadExistingWorlds() {
        for (World world : Bukkit.getWorlds()) {
            managedWorlds.add(world.getName());
            // Try to determine world type - default to NORMAL if unknown
            worldTypeMap.put(world.getName(), WorldTypes.NORMAL);
        }
        plugin.logDebug("Loaded " + managedWorlds.size() + " existing worlds");
    }
    
    /**
     * Populate skyblock world with chests and other features after generation
     * @param world The skyblock world to populate
     * @param worldType The type of skyblock world
     */
    private void populateSkyblockWorld(World world, WorldTypes worldType) {
        Random random = new Random(world.getSeed());
        
        switch (worldType) {
            case SKYBLOCK_CLASSIC:
                new ClassicSkyblockGenerator().populateIsland(world, random, 0, 0);
                break;
            default:
                // Do nothing for other world types
                break;
        }
    }
    
    private boolean deleteWorldFolder(File folder) {
        if (folder == null || !folder.exists()) {
            return false;
        }
        
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteWorldFolder(file);
                } else {
                    file.delete();
                }
            }
        }
        
        return folder.delete();
    }
    
    public static class WorldInfo {
        private final String name;
        private final String type;
        private final int playerCount;
        private final long seed;
        private final World.Environment environment;
        private final boolean hasSpawn;
        private final Location spawnLocation;
        private final int borderRadius;
        private final boolean hasBorder;
        
        public WorldInfo(World world, WorldTypes worldType) {
            this.name = world.getName();
            this.type = worldType != null ? worldType.name() : "UNKNOWN";
            this.playerCount = world.getPlayers().size();
            this.seed = world.getSeed();
            this.environment = world.getEnvironment();
            this.spawnLocation = world.getSpawnLocation();
            this.hasSpawn = spawnLocation != null;
            
            // Get world border info
            WorldBorder border = world.getWorldBorder();
            double borderSize = border.getSize();
            // Default world border size is usually 60,000,000 blocks diameter
            if (borderSize < 60000000) {
                this.hasBorder = true;
                this.borderRadius = (int) (borderSize / 2);
            } else {
                this.hasBorder = false;
                this.borderRadius = -1;
            }
        }
        
        public String getName() { return name; }
        public String getType() { return type; }
        public int getPlayerCount() { return playerCount; }
        public long getSeed() { return seed; }
        public World.Environment getEnvironment() { return environment; }
        public boolean hasSpawn() { return hasSpawn; }
        public Location getSpawnLocation() { return spawnLocation; }
        public int getBorderRadius() { return borderRadius; }
        public boolean hasBorder() { return hasBorder; }
    }
}
