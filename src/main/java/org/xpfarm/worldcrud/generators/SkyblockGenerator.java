package org.xpfarm.worldcrud.generators;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

/**
 * Base Sky block generator for creating floating islands in void worlds.
 * This follows the pattern used by professional sky block plugins like SuperiorSkyblock2
 * and IridiumSkyblock - minimal terrain generation with pure void everywhere except
 * the spawn island.
 */
public abstract class SkyblockGenerator extends ChunkGenerator {
    
    protected static final int SPAWN_CHUNK_X = 0;
    protected static final int SPAWN_CHUNK_Z = 0;
    protected static final int ISLAND_CENTER_X = 8; // Center of spawn chunk (0-15)
    protected static final int ISLAND_CENTER_Z = 8; // Center of spawn chunk (0-15)
    protected static final int ISLAND_HEIGHT = 100; // Y level for island base (traditional sky block height)
    
    @Override
    public void generateNoise(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        // Only generate in spawn chunk - everywhere else remains pure void
        if (chunkX == SPAWN_CHUNK_X && chunkZ == SPAWN_CHUNK_Z) {
            generateIsland(chunkData, random);
        }
        // All other chunks remain completely empty (void)
    }
    
    @Override
    public void generateSurface(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        // Surface generation handled in generateNoise to ensure pure void
    }
    
    @Override
    public void generateBedrock(@NotNull WorldInfo worldInfo, @NotNull Random random, int chunkX, int chunkZ, @NotNull ChunkData chunkData) {
        // No bedrock generation - keeps the void clean
    }
    
    @Override
    public boolean shouldGenerateNoise() {
        return true;
    }
    
    @Override
    public boolean shouldGenerateSurface() {
        return false;
    }
    
    @Override
    public boolean shouldGenerateBedrock() {
        return false;
    }
    
    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }
    
    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }
    
    @Override
    public boolean shouldGenerateMobs() {
        return true; // Allow natural mob spawning on the island
    }
    
    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }
    
    /**
     * Generate the sky block island structure
     * @param chunkData The chunk data to modify
     * @param random Random instance for generation
     */
    protected abstract void generateIsland(ChunkData chunkData, Random random);
    
    /**
     * Place a block relative to the island center
     * @param chunkData The chunk data
     * @param relativeX X coordinate relative to island center
     * @param relativeY Y coordinate relative to island base height
     * @param relativeZ Z coordinate relative to island center
     * @param material The material to place
     */
    protected void placeBlock(ChunkData chunkData, int relativeX, int relativeY, int relativeZ, Material material) {
        int x = ISLAND_CENTER_X + relativeX;
        int y = ISLAND_HEIGHT + relativeY;
        int z = ISLAND_CENTER_Z + relativeZ;
        
        // Ensure coordinates are within chunk boundaries and world height limits
        if (x >= 0 && x < 16 && y >= chunkData.getMinHeight() && y < chunkData.getMaxHeight() && z >= 0 && z < 16) {
            chunkData.setBlock(x, y, z, material);
        }
    }
    
    /**
     * Get the fixed spawn location for sky block worlds
     * @param world The world
     * @param random Random instance (unused but required by interface)
     * @return The spawn location above the island center
     */
    @Override
    public Location getFixedSpawnLocation(@NotNull World world, @NotNull Random random) {
        // Spawn player safely above the island surface
        return new Location(world, ISLAND_CENTER_X + 0.5, ISLAND_HEIGHT + 4, ISLAND_CENTER_Z + 0.5);
    }
    
    /**
     * Populate the world with additional features after terrain generation
     * Called by the WorldManager after the basic terrain is generated
     * @param world The world
     * @param random Random instance
     * @param chunkX Chunk X coordinate
     * @param chunkZ Chunk Z coordinate
     */
    public void populateIsland(World world, Random random, int chunkX, int chunkZ) {
        if (chunkX == SPAWN_CHUNK_X && chunkZ == SPAWN_CHUNK_Z) {
            // Set spawn location to be safe
            Location spawnLocation = getFixedSpawnLocation(world, random);
            world.setSpawnLocation(spawnLocation);
        }
    }
    
    /**
     * Create a chest with starting items at the specified world coordinates
     * @param world The world
     * @param x World X coordinate
     * @param y World Y coordinate  
     * @param z World Z coordinate
     * @param items Items to add to the chest
     */
    protected void createStarterChest(World world, int x, int y, int z, ItemStack... items) {
        Block block = world.getBlockAt(x, y, z);
        block.setType(Material.CHEST);
        
        if (block.getState() instanceof Chest chest) {
            // Clear any existing items
            chest.getInventory().clear();
            
            // Add provided items
            for (ItemStack item : items) {
                if (item != null && !item.getType().isAir()) {
                    chest.getInventory().addItem(item);
                }
            }
            chest.update();
        }
    }
}
