package org.xpfarm.worldcrud.generators;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Traditional Classic Skyblock generator
 * Creates a simple L-shaped dirt/grass island with a tree, following traditional skyblock design.
 * Generates minimal terrain - just empty void with the basic island structure.
 */
public class ClassicSkyblockGenerator extends SkyblockGenerator {
    
    @Override
    protected void generateIsland(ChunkData chunkData, Random random) {
        // Create the classic L-shaped island base (traditional skyblock design)
        // Main 3x3 square platform
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                // Dirt base layer
                placeBlock(chunkData, x, 0, z, Material.DIRT);
                // Grass surface layer
                placeBlock(chunkData, x, 1, z, Material.GRASS_BLOCK);
            }
        }
        
        // Add the "L" extension (1 block extending from corner)
        placeBlock(chunkData, -2, 0, -1, Material.DIRT);
        placeBlock(chunkData, -2, 1, -1, Material.GRASS_BLOCK);
        placeBlock(chunkData, -1, 0, -2, Material.DIRT);
        placeBlock(chunkData, -1, 1, -2, Material.GRASS_BLOCK);
        
        // Place bedrock block underneath for traditional skyblock feel
        placeBlock(chunkData, 0, -1, 0, Material.BEDROCK);
        
        // Generate classic oak tree on the island
        generateClassicTree(chunkData);
        
        // Set biome to plains for optimal growth
        setBiome(chunkData, Biome.PLAINS);
    }
    
    /**
     * Generate a classic oak tree in traditional skyblock style
     */
    private void generateClassicTree(ChunkData chunkData) {
        // Tree trunk (3 blocks high)
        placeBlock(chunkData, 0, 2, 0, Material.OAK_LOG);
        placeBlock(chunkData, 0, 3, 0, Material.OAK_LOG);
        placeBlock(chunkData, 0, 4, 0, Material.OAK_LOG);
        
        // Bottom layer of leaves (5x5 cross pattern)
        for (int x = -2; x <= 2; x++) {
            for (int z = -2; z <= 2; z++) {
                // Skip extreme corners to make natural tree shape
                if (Math.abs(x) == 2 && Math.abs(z) == 2) continue;
                // Skip center where trunk is
                if (x == 0 && z == 0) continue;
                placeBlock(chunkData, x, 4, z, Material.OAK_LEAVES);
            }
        }
        
        // Top layer of leaves (3x3 square)
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                placeBlock(chunkData, x, 5, z, Material.OAK_LEAVES);
            }
        }
        
        // Place a few extra leaves for natural look
        placeBlock(chunkData, 0, 6, 0, Material.OAK_LEAVES);
    }
    
    /**
     * Set biome for the entire chunk to ensure proper grass growth and mob spawning
     */
    private void setBiome(ChunkData chunkData, Biome biome) {
        // Modern Minecraft biome setting would be handled by the server
        // This is a placeholder for biome awareness
    }
    
    @Override
    public void populateIsland(World world, Random random, int chunkX, int chunkZ) {
        super.populateIsland(world, random, chunkX, chunkZ);
        
        if (chunkX == SPAWN_CHUNK_X && chunkZ == SPAWN_CHUNK_Z) {
            // Create traditional skyblock starter chest with essential items
            int chestX = ISLAND_CENTER_X - 2;  // Place on L extension
            int chestY = ISLAND_HEIGHT + 2;
            int chestZ = ISLAND_CENTER_Z - 1;
            
            createStarterChest(world, chestX, chestY, chestZ,
                // Water source (ice blocks that melt)
                new ItemStack(Material.ICE, 2),
                // Lava source for cobble generator
                new ItemStack(Material.LAVA_BUCKET, 1),
                // Food for survival
                new ItemStack(Material.BREAD, 5),
                // Farming essentials
                new ItemStack(Material.RED_MUSHROOM, 1),
                new ItemStack(Material.BROWN_MUSHROOM, 1),
                new ItemStack(Material.PUMPKIN_SEEDS, 1),
                new ItemStack(Material.MELON_SEEDS, 1),
                new ItemStack(Material.WHEAT_SEEDS, 3),
                // Basic resources
                new ItemStack(Material.SUGAR_CANE, 2),
                new ItemStack(Material.CACTUS, 1),
                new ItemStack(Material.BONE_MEAL, 5),
                // Basic tools
                new ItemStack(Material.WOODEN_AXE, 1)
            );
        }
    }
}
