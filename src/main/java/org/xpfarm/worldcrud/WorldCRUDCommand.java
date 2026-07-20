package org.xpfarm.worldcrud;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.*;

public class WorldCRUDCommand implements CommandExecutor, TabCompleter {
    
    private final WorldCRUDPlugin plugin;
    private final WorldManager worldManager;
    private final PermissionHandler permissionHandler;
    
    public WorldCRUDCommand(WorldCRUDPlugin plugin) {
        this.plugin = plugin;
        this.worldManager = plugin.getWorldManager();
        this.permissionHandler = plugin.getPermissionHandler();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create":
                return handleCreateCommand(sender, args);
            case "delete":
                return handleDeleteCommand(sender, args);
            case "rename":
                return handleRenameCommand(sender, args);
            case "teleport":
            case "tp":
                return handleTeleportCommand(sender, args);
            case "tpall":
                return handleTeleportAllCommand(sender, args);
            case "difficulty":
                return handleDifficultyCommand(sender, args);
            case "set":
                return handleSetCommand(sender, args);
            case "setspawn":
                return handleSetSpawnCommand(sender, args);
            case "reset":
                return handleResetCommand(sender, args);
            case "clear":
                return handleClearCommand(sender, args);
            case "save":
                return handleSaveCommand(sender, args);
            case "load":
                return handleLoadCommand(sender, args);
            case "info":
                return handleInfoCommand(sender, args);
            case "list":
                return handleListCommand(sender, args);
            case "help":
                showHelp(sender);
                return true;
            case "version":
                return handleVersionCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender, args);
            case "permissions":
                return handlePermissionsCommand(sender, args);
            case "setpermission":
                return handleSetPermissionCommand(sender, args);
            case "removepermission":
                return handleRemovePermissionCommand(sender, args);
            case "listpermissions":
                return handleListPermissionsCommand(sender, args);
            case "debug":
                return handleDebugCommand(sender, args);
            case "border":
                return handleBorderCommand(sender, args);
            case "sticky":
                return handleStickyCommand(sender, args);
            case "playerdata":
                return handlePlayerDataCommand(sender, args);
            default:
                plugin.sendMessage(sender, "Unknown subcommand: " + subCommand, NamedTextColor.RED);
                showHelp(sender);
                return true;
        }
    }
    
    private boolean handleCreateCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (args.length < 3) {
            plugin.sendMessage(sender, "Usage: /worldcrud create <name> <type> [size]", NamedTextColor.YELLOW);
            plugin.sendMessage(sender, "Available types: " + String.join(", ", WorldTypes.getValidTypes()), NamedTextColor.GRAY);
            plugin.sendMessage(sender, "Optional size: World border radius in blocks (e.g., 1000)", NamedTextColor.GRAY);
            return true;
        }
        
        String worldName = args[1];
        WorldTypes worldType = WorldTypes.fromString(args[2]);
        
        int borderSize = -1;
        if (args.length >= 4) {
            try {
                borderSize = Integer.parseInt(args[3]);
                if (borderSize <= 0) {
                    plugin.sendMessage(sender, "Border size must be a positive integer", NamedTextColor.RED);
                    return true;
                }
            } catch (NumberFormatException e) {
                plugin.sendMessage(sender, "Invalid border size. Must be a number", NamedTextColor.RED);
                return true;
            }
        }
        
        if (worldManager.createWorld(worldName, worldType, borderSize)) {
            String message = "Successfully created world: " + worldName + " (" + worldType + ")";
            if (borderSize > 0) {
                message += " with border radius: " + borderSize + " blocks";
            }
            plugin.sendMessage(sender, message, NamedTextColor.GREEN);
        } else {
            plugin.sendMessage(sender, "Failed to create world: " + worldName, NamedTextColor.RED);
        }
        
        return true;
    }
    
    private boolean handleDeleteCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (args.length < 2) {
            plugin.sendMessage(sender, "Usage: /worldcrud delete <name>", NamedTextColor.YELLOW);
            return true;
        }
        
        String worldName = args[1];
        
        if (worldManager.deleteWorld(worldName)) {
            plugin.sendMessage(sender, "Successfully deleted world: " + worldName, NamedTextColor.GREEN);
        } else {
            plugin.sendMessage(sender, "Failed to delete world: " + worldName, NamedTextColor.RED);
        }
        
        return true;
    }
    
    private boolean handleRenameCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (args.length < 3) {
            plugin.sendMessage(sender, "Usage: /worldcrud rename <old_name> <new_name>", NamedTextColor.YELLOW);
            return true;
        }
        
        String oldName = args[1];
        String newName = args[2];
        
        if (worldManager.renameWorld(oldName, newName)) {
            plugin.sendMessage(sender, "Successfully renamed world from " + oldName + " to " + newName, NamedTextColor.GREEN);
        } else {
            plugin.sendMessage(sender, "Failed to rename world from " + oldName + " to " + newName, NamedTextColor.RED);
        }
        
        return true;
    }
    
    private boolean handleTeleportCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasTeleportPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.TELEPORT_PERMISSION);
            return true;
        }
        
        if (args.length < 2) {
            plugin.sendMessage(sender, "Usage: /worldcrud tp <world_name> [player]", NamedTextColor.YELLOW);
            plugin.sendMessage(sender, "       /worldcrud tp <world_name> - Teleport yourself", NamedTextColor.GRAY);
            plugin.sendMessage(sender, "       /worldcrud tp <world_name> <player> - Teleport another player", NamedTextColor.GRAY);
            return true;
        }
        
        String worldName = args[1];
        
        // If 3rd argument (player) provided
        if (args.length >= 3) {
            if (!permissionHandler.hasAdminPermission(sender)) {
                permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
                return true;
            }
            
            String targetPlayerName = args[2];
            Player targetPlayer = PlayerLookup.resolveAllowingPartial(targetPlayerName).orElse(null);

            if (targetPlayer == null) {
                plugin.sendMessage(sender, PlayerLookup.noSuchPlayerMessage(targetPlayerName, PlayerLookup.onlineNames()), NamedTextColor.RED);
                return true;
            }

            if (worldManager.teleportToWorld(targetPlayer, worldName)) {
                plugin.sendMessage(sender, "Successfully teleported " + targetPlayer.getName() + " to world: " + worldName, NamedTextColor.GREEN);
                plugin.sendMessage(targetPlayer, "You have been teleported to world: " + worldName, NamedTextColor.GREEN);
            } else {
                plugin.sendMessage(sender, "Failed to teleport " + targetPlayer.getName() + " to world: " + worldName, NamedTextColor.RED);
            }
            
            return true;
        }
        
        // Self teleportation
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "Console cannot teleport itself. Specify a player: /worldcrud tp <world_name> <player>", NamedTextColor.RED);
            return true;
        }
        
        Player player = (Player) sender;
        
        if (worldManager.teleportToWorld(player, worldName)) {
            plugin.sendMessage(sender, "Successfully teleported to world: " + worldName, NamedTextColor.GREEN);
        } else {
            plugin.sendMessage(sender, "Failed to teleport to world: " + worldName, NamedTextColor.RED);
        }
        
        return true;
    }
    
    private boolean handleTeleportAllCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (args.length < 2) {
            plugin.sendMessage(sender, "Usage: /worldcrud tpall <world_name>", NamedTextColor.YELLOW);
            return true;
        }
        
        String worldName = args[1];
        World targetWorld = Bukkit.getWorld(worldName);
        
        if (targetWorld == null) {
            plugin.sendMessage(sender, "World not found: " + worldName, NamedTextColor.RED);
            return true;
        }
        
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        if (onlinePlayers.isEmpty()) {
            plugin.sendMessage(sender, "No players online to teleport", NamedTextColor.YELLOW);
            return true;
        }
        
        int teleportCount = 0;
        for (Player player : onlinePlayers) {
            if (worldManager.teleportToWorld(player, worldName)) {
                teleportCount++;
                plugin.sendMessage(player, "You have been teleported to world: " + worldName, NamedTextColor.GREEN);
            }
        }
        
        plugin.sendMessage(sender, "Successfully teleported " + teleportCount + " players to world: " + worldName, NamedTextColor.GREEN);
        return true;
    }
    
    private boolean handleDifficultyCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (args.length < 2) {
            plugin.sendMessage(sender, "Usage: /worldcrud difficulty <difficulty> [player]", NamedTextColor.YELLOW);
            plugin.sendMessage(sender, "       /worldcrud difficulty <difficulty> - Set difficulty for all players in current world", NamedTextColor.GRAY);
            plugin.sendMessage(sender, "       /worldcrud difficulty <difficulty> <player> - Set difficulty for specific player's world", NamedTextColor.GRAY);
            plugin.sendMessage(sender, "Available difficulties: peaceful, easy, normal, hard", NamedTextColor.GRAY);
            return true;
        }
        
        String difficultyString = args[1].toLowerCase();
        Difficulty difficulty;
        
        try {
            switch (difficultyString) {
                case "peaceful":
                case "0":
                    difficulty = Difficulty.PEACEFUL;
                    break;
                case "easy":
                case "1":
                    difficulty = Difficulty.EASY;
                    break;
                case "normal":
                case "2":
                    difficulty = Difficulty.NORMAL;
                    break;
                case "hard":
                case "3":
                    difficulty = Difficulty.HARD;
                    break;
                default:
                    plugin.sendMessage(sender, "Invalid difficulty: " + difficultyString, NamedTextColor.RED);
                    plugin.sendMessage(sender, "Available difficulties: peaceful, easy, normal, hard", NamedTextColor.GRAY);
                    return true;
            }
        } catch (Exception e) {
            plugin.sendMessage(sender, "Invalid difficulty: " + difficultyString, NamedTextColor.RED);
            return true;
        }
        
        // If player specified, set difficulty for that player's world
        if (args.length >= 3) {
            String targetPlayerName = args[2];
            Player targetPlayer = PlayerLookup.resolveAllowingPartial(targetPlayerName).orElse(null);

            if (targetPlayer == null) {
                plugin.sendMessage(sender, PlayerLookup.noSuchPlayerMessage(targetPlayerName, PlayerLookup.onlineNames()), NamedTextColor.RED);
                return true;
            }

            World playerWorld = targetPlayer.getWorld();
            playerWorld.setDifficulty(difficulty);
            
            plugin.sendMessage(sender, "Set difficulty to " + difficulty.name().toLowerCase() + " for world: " + playerWorld.getName() + " (player " + targetPlayer.getName() + ")", NamedTextColor.GREEN);
            plugin.sendMessage(targetPlayer, "World difficulty has been set to: " + difficulty.name().toLowerCase(), NamedTextColor.GREEN);
            
            return true;
        }
        
        // Set difficulty for sender's current world or all worlds if console
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "Console must specify a player: /worldcrud difficulty <difficulty> <player>", NamedTextColor.RED);
            return true;
        }
        
        Player player = (Player) sender;
        World currentWorld = player.getWorld();
        currentWorld.setDifficulty(difficulty);
        
        plugin.sendMessage(sender, "Set difficulty to " + difficulty.name().toLowerCase() + " for world: " + currentWorld.getName(), NamedTextColor.GREEN);
        return true;
    }
    
    private boolean handleSetCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (args.length < 4) {
            plugin.sendMessage(sender, "Usage: /worldcrud set <world_name> <setting> <value>", NamedTextColor.YELLOW);
            plugin.sendMessage(sender, "Available settings:", NamedTextColor.GRAY);
            plugin.sendMessage(sender, "  difficulty <peaceful|easy|normal|hard> - Set world difficulty", NamedTextColor.WHITE);
            plugin.sendMessage(sender, "  weather <clear|rain|thunder> - Set weather", NamedTextColor.WHITE);
            plugin.sendMessage(sender, "  time <day|night|number> - Set time of day", NamedTextColor.WHITE);
            plugin.sendMessage(sender, "  pvp <true|false> - Enable/disable PvP", NamedTextColor.WHITE);
            plugin.sendMessage(sender, "  gamemode <survival|creative|adventure|spectator> - Set default gamemode", NamedTextColor.WHITE);
            return true;
        }
        
        String worldName = args[1];
        String setting = args[2].toLowerCase();
        String value = args[3];
        
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.sendMessage(sender, "World not found: " + worldName, NamedTextColor.RED);
            return true;
        }
        
        switch (setting) {
            case "difficulty":
                return handleSetDifficulty(sender, world, value);
            case "weather":
                return handleSetWeather(sender, world, value);
            case "time":
                return handleSetTime(sender, world, value);
            case "pvp":
                return handleSetPvP(sender, world, value);
            case "gamemode":
                return handleSetGameMode(sender, world, value);
            default:
                plugin.sendMessage(sender, "Unknown setting: " + setting, NamedTextColor.RED);
                plugin.sendMessage(sender, "Available settings: difficulty, weather, time, pvp, gamemode", NamedTextColor.GRAY);
                return true;
        }
    }
    
    private boolean handleSetDifficulty(CommandSender sender, World world, String value) {
        try {
            Difficulty difficulty;
            switch (value.toLowerCase()) {
                case "peaceful":
                case "0":
                    difficulty = Difficulty.PEACEFUL;
                    break;
                case "easy":
                case "1":
                    difficulty = Difficulty.EASY;
                    break;
                case "normal":
                case "2":
                    difficulty = Difficulty.NORMAL;
                    break;
                case "hard":
                case "3":
                    difficulty = Difficulty.HARD;
                    break;
                default:
                    plugin.sendMessage(sender, "Invalid difficulty: " + value, NamedTextColor.RED);
                    plugin.sendMessage(sender, "Available difficulties: peaceful, easy, normal, hard", NamedTextColor.GRAY);
                    return true;
            }
            
            world.setDifficulty(difficulty);
            plugin.sendMessage(sender, "Set difficulty to " + difficulty.name().toLowerCase() + " for world: " + world.getName(), NamedTextColor.GREEN);
            return true;
        } catch (Exception e) {
            plugin.sendMessage(sender, "Failed to set difficulty for world: " + world.getName(), NamedTextColor.RED);
            return true;
        }
    }
    
    private boolean handleSetWeather(CommandSender sender, World world, String value) {
        try {
            switch (value.toLowerCase()) {
                case "clear":
                case "sun":
                case "sunny":
                    world.setStorm(false);
                    world.setThundering(false);
                    break;
                case "rain":
                case "rainy":
                    world.setStorm(true);
                    world.setThundering(false);
                    break;
                case "thunder":
                case "thundering":
                case "storm":
                    world.setStorm(true);
                    world.setThundering(true);
                    break;
                default:
                    plugin.sendMessage(sender, "Invalid weather: " + value, NamedTextColor.RED);
                    plugin.sendMessage(sender, "Available weather: clear, rain, thunder", NamedTextColor.GRAY);
                    return true;
            }
            
            plugin.sendMessage(sender, "Set weather to " + value.toLowerCase() + " for world: " + world.getName(), NamedTextColor.GREEN);
            return true;
        } catch (Exception e) {
            plugin.sendMessage(sender, "Failed to set weather for world: " + world.getName(), NamedTextColor.RED);
            return true;
        }
    }
    
    private boolean handleSetTime(CommandSender sender, World world, String value) {
        try {
            long time;
            switch (value.toLowerCase()) {
                case "day":
                case "morning":
                    time = 1000;
                    break;
                case "noon":
                case "midday":
                    time = 6000;
                    break;
                case "night":
                case "evening":
                    time = 13000;
                    break;
                case "midnight":
                    time = 18000;
                    break;
                default:
                    try {
                        time = Long.parseLong(value);
                    } catch (NumberFormatException e) {
                        plugin.sendMessage(sender, "Invalid time: " + value, NamedTextColor.RED);
                        plugin.sendMessage(sender, "Use: day, noon, night, midnight, or a number (0-24000)", NamedTextColor.GRAY);
                        return true;
                    }
                    break;
            }
            
            world.setTime(time);
            plugin.sendMessage(sender, "Set time to " + time + " for world: " + world.getName(), NamedTextColor.GREEN);
            return true;
        } catch (Exception e) {
            plugin.sendMessage(sender, "Failed to set time for world: " + world.getName(), NamedTextColor.RED);
            return true;
        }
    }
    
    private boolean handleSetPvP(CommandSender sender, World world, String value) {
        try {
            boolean pvpEnabled;
            switch (value.toLowerCase()) {
                case "true":
                case "on":
                case "enable":
                case "enabled":
                case "yes":
                    pvpEnabled = true;
                    break;
                case "false":
                case "off":
                case "disable":
                case "disabled":
                case "no":
                    pvpEnabled = false;
                    break;
                default:
                    plugin.sendMessage(sender, "Invalid PvP setting: " + value, NamedTextColor.RED);
                    plugin.sendMessage(sender, "Use: true/false, on/off, enable/disable", NamedTextColor.GRAY);
                    return true;
            }
            
            world.setPVP(pvpEnabled);
            plugin.sendMessage(sender, "Set PvP to " + (pvpEnabled ? "enabled" : "disabled") + " for world: " + world.getName(), NamedTextColor.GREEN);
            return true;
        } catch (Exception e) {
            plugin.sendMessage(sender, "Failed to set PvP for world: " + world.getName(), NamedTextColor.RED);
            return true;
        }
    }
    
    private boolean handleSetGameMode(CommandSender sender, World world, String value) {
        try {
            GameMode gameMode;
            switch (value.toLowerCase()) {
                case "survival":
                case "s":
                case "0":
                    gameMode = GameMode.SURVIVAL;
                    break;
                case "creative":
                case "c":
                case "1":
                    gameMode = GameMode.CREATIVE;
                    break;
                case "adventure":
                case "a":
                case "2":
                    gameMode = GameMode.ADVENTURE;
                    break;
                case "spectator":
                case "sp":
                case "3":
                    gameMode = GameMode.SPECTATOR;
                    break;
                default:
                    plugin.sendMessage(sender, "Invalid gamemode: " + value, NamedTextColor.RED);
                    plugin.sendMessage(sender, "Available gamemodes: survival, creative, adventure, spectator", NamedTextColor.GRAY);
                    return true;
            }
            
            // Note: Setting default gamemode for a world requires changing the world's properties
            // This is a basic implementation - in a full plugin you might want to store this in config
            plugin.sendMessage(sender, "Note: This sets the default gamemode preference for world: " + world.getName(), NamedTextColor.YELLOW);
            plugin.sendMessage(sender, "Gamemode setting " + gameMode.name().toLowerCase() + " noted for world: " + world.getName(), NamedTextColor.GREEN);
            
            // For demonstration, we could change all current players in the world to this gamemode
            for (Player player : world.getPlayers()) {
                player.setGameMode(gameMode);
                plugin.sendMessage(player, "Your gamemode has been changed to: " + gameMode.name().toLowerCase(), NamedTextColor.GREEN);
            }
            
            return true;
        } catch (Exception e) {
            plugin.sendMessage(sender, "Failed to set gamemode for world: " + world.getName(), NamedTextColor.RED);
            return true;
        }
    }
    
    private boolean handleSetSpawnCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, "This command can only be used by players", NamedTextColor.RED);
            return true;
        }
        
        if (args.length < 2) {
            plugin.sendMessage(sender, "Usage: /worldcrud setspawn <world_name>", NamedTextColor.YELLOW);
            return true;
        }
        
        Player player = (Player) sender;
        String worldName = args[1];
        Location location = player.getLocation();
        
        if (worldManager.setWorldSpawn(worldName, location)) {
            plugin.sendMessage(sender, "Successfully set spawn for world: " + worldName, NamedTextColor.GREEN);
        } else {
            plugin.sendMessage(sender, "Failed to set spawn for world: " + worldName, NamedTextColor.RED);
        }
        
        return true;
    }
    
    private boolean handleResetCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (args.length < 2) {
            plugin.sendMessage(sender, "Usage: /worldcrud reset <world_name>", NamedTextColor.YELLOW);
            return true;
        }
        
        String worldName = args[1];
        
        if (worldManager.resetWorld(worldName)) {
            plugin.sendMessage(sender, "Successfully reset world: " + worldName, NamedTextColor.GREEN);
        } else {
            plugin.sendMessage(sender, "Failed to reset world: " + worldName, NamedTextColor.RED);
        }
        
        return true;
    }
    
    private boolean handleClearCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (args.length < 2) {
            plugin.sendMessage(sender, "Usage: /worldcrud clear <world_name>", NamedTextColor.YELLOW);
            return true;
        }
        
        String worldName = args[1];
        
        if (worldManager.clearWorld(worldName)) {
            plugin.sendMessage(sender, "Successfully cleared entities from world: " + worldName, NamedTextColor.GREEN);
        } else {
            plugin.sendMessage(sender, "Failed to clear entities from world: " + worldName, NamedTextColor.RED);
        }
        
        return true;
    }
    
    private boolean handleSaveCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (args.length < 2) {
            plugin.sendMessage(sender, "Usage: /worldcrud save <world_name>", NamedTextColor.YELLOW);
            return true;
        }
        
        String worldName = args[1];
        
        if (worldManager.saveWorld(worldName)) {
            plugin.sendMessage(sender, "Successfully saved world: " + worldName, NamedTextColor.GREEN);
        } else {
            plugin.sendMessage(sender, "Failed to save world: " + worldName, NamedTextColor.RED);
        }
        
        return true;
    }
    
    private boolean handleLoadCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (args.length < 2) {
            plugin.sendMessage(sender, "Usage: /worldcrud load <world_name>", NamedTextColor.YELLOW);
            return true;
        }
        
        String worldName = args[1];
        
        if (worldManager.loadWorld(worldName)) {
            plugin.sendMessage(sender, "Successfully loaded world: " + worldName, NamedTextColor.GREEN);
        } else {
            plugin.sendMessage(sender, "Failed to load world: " + worldName, NamedTextColor.RED);
        }
        
        return true;
    }
    
    private boolean handleInfoCommand(CommandSender sender, String[] args) {
        String worldName;
        
        if (args.length < 2) {
            if (sender instanceof Player) {
                worldName = ((Player) sender).getWorld().getName();
            } else {
                plugin.sendMessage(sender, "Usage: /worldcrud info <world_name>", NamedTextColor.YELLOW);
                return true;
            }
        } else {
            worldName = args[1];
        }
        
        WorldManager.WorldInfo info = worldManager.getWorldInfo(worldName);
        if (info != null) {
            showWorldInfo(sender, info);
        } else {
            plugin.sendMessage(sender, "World not found: " + worldName, NamedTextColor.RED);
        }
        
        return true;
    }
    
    private boolean handleListCommand(CommandSender sender, String[] args) {
        List<String> worlds = worldManager.getWorldList();
        
        sender.sendMessage(Component.text("=== World List ===", NamedTextColor.GOLD));
        if (worlds.isEmpty()) {
            sender.sendMessage(Component.text("No worlds found", NamedTextColor.GRAY));
        } else {
            sender.sendMessage(Component.text("Available worlds:", NamedTextColor.AQUA));
            for (String world : worlds) {
                sender.sendMessage(Component.text("• " + world, NamedTextColor.WHITE));
            }
        }
        
        return true;
    }
    
    private boolean handleVersionCommand(CommandSender sender, String[] args) {
        String version = plugin.getDescription().getVersion();
        sender.sendMessage(Component.text("WorldCRUD Plugin", NamedTextColor.GOLD)
                .append(Component.text(" v" + version, NamedTextColor.YELLOW)));
        sender.sendMessage(Component.text("Author: ", NamedTextColor.GRAY)
                .append(Component.text("Carmelo Santana", NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Website: ", NamedTextColor.GRAY)
                .append(Component.text("https://xpfarm.org", NamedTextColor.AQUA)));
        return true;
    }
    
    private boolean handleReloadCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        plugin.reloadPluginConfig();
        plugin.sendMessage(sender, "Configuration reloaded successfully", NamedTextColor.GREEN);
        return true;
    }
    
    private boolean handlePermissionsCommand(CommandSender sender, String[] args) {
        permissionHandler.showPermissionStructure(sender);
        return true;
    }
    
    private boolean handleSetPermissionCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (args.length < 3) {
            plugin.sendMessage(sender, "Usage: /worldcrud setpermission <player> <permission>", NamedTextColor.YELLOW);
            return true;
        }
        
        String playerName = args[1];
        String permission = args[2];
        Player target = PlayerLookup.resolveAllowingPartial(playerName).orElse(null);

        if (target == null) {
            plugin.sendMessage(sender, PlayerLookup.noSuchPlayerMessage(playerName, PlayerLookup.onlineNames()), NamedTextColor.RED);
            return true;
        }

        permissionHandler.addPermission(target, permission);
        plugin.sendMessage(sender, "Added permission " + permission + " to " + playerName, NamedTextColor.GREEN);
        return true;
    }
    
    private boolean handleRemovePermissionCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (args.length < 3) {
            plugin.sendMessage(sender, "Usage: /worldcrud removepermission <player> <permission>", NamedTextColor.YELLOW);
            return true;
        }
        
        String playerName = args[1];
        String permission = args[2];
        Player target = PlayerLookup.resolveAllowingPartial(playerName).orElse(null);

        if (target == null) {
            plugin.sendMessage(sender, PlayerLookup.noSuchPlayerMessage(playerName, PlayerLookup.onlineNames()), NamedTextColor.RED);
            return true;
        }

        permissionHandler.removePermission(target, permission);
        plugin.sendMessage(sender, "Removed permission " + permission + " from " + playerName, NamedTextColor.GREEN);
        return true;
    }
    
    private boolean handleListPermissionsCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (args.length < 2) {
            plugin.sendMessage(sender, "Usage: /worldcrud listpermissions <player>", NamedTextColor.YELLOW);
            return true;
        }
        
        String playerName = args[1];
        Player target = PlayerLookup.resolveAllowingPartial(playerName).orElse(null);

        if (target == null) {
            plugin.sendMessage(sender, PlayerLookup.noSuchPlayerMessage(playerName, PlayerLookup.onlineNames()), NamedTextColor.RED);
            return true;
        }

        Set<String> permissions = permissionHandler.getPlayerPermissions(target);
        sender.sendMessage(Component.text("=== Permissions for " + playerName + " ===", NamedTextColor.GOLD));
        if (permissions.isEmpty()) {
            sender.sendMessage(Component.text("No custom permissions set", NamedTextColor.GRAY));
        } else {
            for (String perm : permissions) {
                sender.sendMessage(Component.text("• " + perm, NamedTextColor.WHITE));
            }
        }
        
        return true;
    }
    
    private boolean handleDebugCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (args.length < 2) {
            plugin.sendMessage(sender, "Usage: /worldcrud debug <on|off>", NamedTextColor.YELLOW);
            return true;
        }
        
        String mode = args[1].toLowerCase();
        if (mode.equals("on") || mode.equals("true")) {
            plugin.setDebugMode(true);
            plugin.sendMessage(sender, "Debug mode enabled", NamedTextColor.GREEN);
        } else if (mode.equals("off") || mode.equals("false")) {
            plugin.setDebugMode(false);
            plugin.sendMessage(sender, "Debug mode disabled", NamedTextColor.GREEN);
        } else {
            plugin.sendMessage(sender, "Usage: /worldcrud debug <on|off>", NamedTextColor.YELLOW);
        }
        
        return true;
    }
    
    private boolean handleBorderCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (args.length < 3) {
            plugin.sendMessage(sender, "Usage: /worldcrud border <world_name> <size>", NamedTextColor.YELLOW);
            plugin.sendMessage(sender, "Size: World border radius in blocks (e.g., 1000)", NamedTextColor.GRAY);
            return true;
        }
        
        String worldName = args[1];
        int borderSize;
        
        try {
            borderSize = Integer.parseInt(args[2]);
            if (borderSize <= 0) {
                plugin.sendMessage(sender, "Border size must be a positive integer", NamedTextColor.RED);
                return true;
            }
        } catch (NumberFormatException e) {
            plugin.sendMessage(sender, "Invalid border size. Must be a number", NamedTextColor.RED);
            return true;
        }
        
        if (worldManager.setWorldBorder(worldName, borderSize)) {
            plugin.sendMessage(sender, "Successfully set world border for " + worldName + " to " + borderSize + " blocks radius", NamedTextColor.GREEN);
        } else {
            plugin.sendMessage(sender, "Failed to set world border for: " + worldName, NamedTextColor.RED);
        }

        return true;
    }
    
    private boolean handleStickyCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (args.length < 2) {
            plugin.sendMessage(sender, "Usage: /worldcrud sticky <enable|disable|status|world>", NamedTextColor.YELLOW);
            plugin.sendMessage(sender, "  enable  - Enable sticky logout globally", NamedTextColor.GRAY);
            plugin.sendMessage(sender, "  disable - Disable sticky logout globally", NamedTextColor.GRAY);
            plugin.sendMessage(sender, "  status  - Show sticky logout status", NamedTextColor.GRAY);
            plugin.sendMessage(sender, "  world <world> <enable|disable> - Configure per-world sticky logout", NamedTextColor.GRAY);
            return true;
        }
        
        String subCommand = args[1].toLowerCase();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        
        switch (subCommand) {
            case "enable":
                plugin.getConfig().set("sticky-logout.enabled", true);
                plugin.saveConfig();
                plugin.sendMessage(sender, "Sticky logout enabled globally", NamedTextColor.GREEN);
                break;
                
            case "disable":
                plugin.getConfig().set("sticky-logout.enabled", false);
                plugin.saveConfig();
                plugin.sendMessage(sender, "Sticky logout disabled globally", NamedTextColor.YELLOW);
                break;
                
            case "status":
                boolean globalEnabled = playerDataManager.isStickyLogoutEnabled();
                int trackedPlayers = playerDataManager.getTrackedPlayerCount();
                
                plugin.sendMessage(sender, "=== Sticky Logout Status ===", NamedTextColor.GOLD);
                plugin.sendMessage(sender, "Global Status: " + (globalEnabled ? "Enabled" : "Disabled"), 
                                 globalEnabled ? NamedTextColor.GREEN : NamedTextColor.RED);
                plugin.sendMessage(sender, "Tracked Players: " + trackedPlayers, NamedTextColor.AQUA);
                break;
                
            case "world":
                if (args.length < 4) {
                    plugin.sendMessage(sender, "Usage: /worldcrud sticky world <world_name> <enable|disable>", NamedTextColor.YELLOW);
                    return true;
                }
                
                String worldName = args[2];
                String action = args[3].toLowerCase();
                
                if (!action.equals("enable") && !action.equals("disable")) {
                    plugin.sendMessage(sender, "Action must be 'enable' or 'disable'", NamedTextColor.RED);
                    return true;
                }
                
                boolean enable = action.equals("enable");
                plugin.getConfig().set("sticky-logout.worlds." + worldName + ".enabled", enable);
                plugin.saveConfig();
                
                plugin.sendMessage(sender, "Sticky logout " + action + "d for world: " + worldName, 
                                 enable ? NamedTextColor.GREEN : NamedTextColor.YELLOW);
                break;
                
            default:
                plugin.sendMessage(sender, "Unknown sticky subcommand: " + subCommand, NamedTextColor.RED);
                plugin.sendMessage(sender, "Use: enable, disable, status, or world", NamedTextColor.GRAY);
                break;
        }
        
        return true;
    }
    
    private boolean handlePlayerDataCommand(CommandSender sender, String[] args) {
        if (!permissionHandler.hasAdminPermission(sender)) {
            permissionHandler.sendNoPermissionMessage(sender, PermissionHandler.ADMIN_PERMISSION);
            return true;
        }
        
        if (args.length < 2) {
            plugin.sendMessage(sender, "Usage: /worldcrud playerdata <list|clear|player>", NamedTextColor.YELLOW);
            plugin.sendMessage(sender, "  list   - List all tracked players", NamedTextColor.GRAY);
            plugin.sendMessage(sender, "  clear  - Clear all player data", NamedTextColor.GRAY);
            plugin.sendMessage(sender, "  player <name> - Show specific player data", NamedTextColor.GRAY);
            return true;
        }
        
        String subCommand = args[1].toLowerCase();
        PlayerDataManager playerDataManager = plugin.getPlayerDataManager();
        
        switch (subCommand) {
            case "list":
                int count = playerDataManager.getTrackedPlayerCount();
                plugin.sendMessage(sender, "=== Player Data Summary ===", NamedTextColor.GOLD);
                plugin.sendMessage(sender, "Total tracked players: " + count, NamedTextColor.AQUA);
                
                if (count > 0) {
                    plugin.sendMessage(sender, "Use '/worldcrud playerdata player <name>' for specific details", NamedTextColor.GRAY);
                }
                break;
                
            case "clear":
                playerDataManager.clearAllData();
                plugin.sendMessage(sender, "All player data cleared", NamedTextColor.YELLOW);
                break;
                
            case "player":
                if (args.length < 3) {
                    plugin.sendMessage(sender, "Usage: /worldcrud playerdata player <player_name>", NamedTextColor.YELLOW);
                    return true;
                }
                
                String playerName = args[2];
                Player targetPlayer = PlayerLookup.resolveAllowingPartial(playerName).orElse(null);
                
                if (targetPlayer != null) {
                    String lastWorld = playerDataManager.getLastWorld(targetPlayer);
                    String currentWorld = targetPlayer.getWorld().getName();
                    boolean stickyEnabled = playerDataManager.isStickyLogoutEnabled(currentWorld);
                    
                    plugin.sendMessage(sender, "=== Player Data: " + targetPlayer.getName() + " ===", NamedTextColor.GOLD);
                    plugin.sendMessage(sender, "Current World: " + currentWorld, NamedTextColor.AQUA);
                    plugin.sendMessage(sender, "Last World: " + (lastWorld != null ? lastWorld : "None"), NamedTextColor.AQUA);
                    plugin.sendMessage(sender, "Sticky Logout: " + (stickyEnabled ? "Enabled" : "Disabled"), 
                                     stickyEnabled ? NamedTextColor.GREEN : NamedTextColor.RED);
                } else {
                    plugin.sendMessage(sender, PlayerLookup.noSuchPlayerMessage(playerName, PlayerLookup.onlineNames()), NamedTextColor.RED);
                }
                break;
                
            default:
                plugin.sendMessage(sender, "Unknown playerdata subcommand: " + subCommand, NamedTextColor.RED);
                plugin.sendMessage(sender, "Use: list, clear, or player", NamedTextColor.GRAY);
                break;
        }
        
        return true;
    }
    
    private void showHelp(CommandSender sender) {
        sender.sendMessage(Component.text("=== WorldCRUD Commands ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/worldcrud create <name> <type> [size]", NamedTextColor.YELLOW)
                .append(Component.text(" - Create a new world", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("  Available types: ", NamedTextColor.AQUA)
                .append(Component.text(String.join(", ", WorldTypes.getValidTypes()), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("  Optional size: World border radius in blocks", NamedTextColor.AQUA));
        sender.sendMessage(Component.text("/worldcrud delete <name>", NamedTextColor.YELLOW)
                .append(Component.text(" - Delete a world", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud rename <old> <new>", NamedTextColor.YELLOW)
                .append(Component.text(" - Rename a world", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud teleport <name>", NamedTextColor.YELLOW)
                .append(Component.text(" - Teleport to a world", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud tp <n> [player]", NamedTextColor.YELLOW)
                .append(Component.text(" - Teleport yourself or another player", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud tpall <n>", NamedTextColor.YELLOW)
                .append(Component.text(" - Teleport all players to a world", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud difficulty <level> [player]", NamedTextColor.YELLOW)
                .append(Component.text(" - Set world difficulty", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud set <n> <setting> <value>", NamedTextColor.YELLOW)
                .append(Component.text(" - Set world properties", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud setspawn <name>", NamedTextColor.YELLOW)
                .append(Component.text(" - Set spawn point", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud reset <name>", NamedTextColor.YELLOW)
                .append(Component.text(" - Reset a world", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud clear <name>", NamedTextColor.YELLOW)
                .append(Component.text(" - Clear entities", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud save <name>", NamedTextColor.YELLOW)
                .append(Component.text(" - Save a world", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud load <name>", NamedTextColor.YELLOW)
                .append(Component.text(" - Load a world", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud info [name]", NamedTextColor.YELLOW)
                .append(Component.text(" - Show world info", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud list", NamedTextColor.YELLOW)
                .append(Component.text(" - List all worlds", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud help", NamedTextColor.YELLOW)
                .append(Component.text(" - Show this help", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud version", NamedTextColor.YELLOW)
                .append(Component.text(" - Show version", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud reload", NamedTextColor.YELLOW)
                .append(Component.text(" - Reload config", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud permissions", NamedTextColor.YELLOW)
                .append(Component.text(" - Show permissions", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud debug <on|off>", NamedTextColor.YELLOW)
                .append(Component.text(" - Toggle debug mode", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud border <name> <size>", NamedTextColor.YELLOW)
                .append(Component.text(" - Set world border", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud sticky <enable|disable|status|world>", NamedTextColor.YELLOW)
                .append(Component.text(" - Manage sticky logout", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/worldcrud playerdata <list|clear|player>", NamedTextColor.YELLOW)
                .append(Component.text(" - Manage player data", NamedTextColor.GRAY)));
    }
    
    private void showWorldInfo(CommandSender sender, WorldManager.WorldInfo info) {
        sender.sendMessage(Component.text("=== World Information: " + info.getName() + " ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("Type: ", NamedTextColor.AQUA)
                .append(Component.text(info.getType(), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Environment: ", NamedTextColor.AQUA)
                .append(Component.text(info.getEnvironment().name(), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Players: ", NamedTextColor.AQUA)
                .append(Component.text(String.valueOf(info.getPlayerCount()), NamedTextColor.WHITE)));
        sender.sendMessage(Component.text("Seed: ", NamedTextColor.AQUA)
                .append(Component.text(String.valueOf(info.getSeed()), NamedTextColor.WHITE)));
        
        if (info.hasSpawn()) {
            Location spawn = info.getSpawnLocation();
            sender.sendMessage(Component.text("Spawn: ", NamedTextColor.AQUA)
                    .append(Component.text(String.format("%.1f, %.1f, %.1f", spawn.getX(), spawn.getY(), spawn.getZ()), NamedTextColor.WHITE)));
        } else {
            sender.sendMessage(Component.text("Spawn: ", NamedTextColor.AQUA)
                    .append(Component.text("Not set", NamedTextColor.GRAY)));
        }
        
        if (info.hasBorder()) {
            sender.sendMessage(Component.text("World Border: ", NamedTextColor.AQUA)
                    .append(Component.text(info.getBorderRadius() + " blocks radius", NamedTextColor.WHITE)));
        } else {
            sender.sendMessage(Component.text("World Border: ", NamedTextColor.AQUA)
                    .append(Component.text("None set", NamedTextColor.GRAY)));
        }
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            // Main subcommands
            completions.addAll(Arrays.asList(
                    "create", "delete", "rename", "teleport", "tp", "tpall", "difficulty", "set", "setspawn", "reset", "clear", "save", "load",
                    "info", "list", "help", "version", "reload", "permissions", "setpermission", "removepermission", "listpermissions", "debug", "border", "sticky", "playerdata"
            ));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "create":
                    // No completion for world name (user input)
                    break;
                case "delete":
                case "rename":
                case "teleport":
                case "tp":
                case "tpall":
                case "setspawn":
                case "reset":
                case "clear":
                case "save":
                case "load":
                case "info":
                case "set":
                    completions.addAll(worldManager.getWorldList());
                    break;
                case "difficulty":
                    completions.addAll(Arrays.asList("peaceful", "easy", "normal", "hard"));
                    break;
                case "setpermission":
                case "removepermission":
                case "listpermissions":
                    completions.addAll(getOnlinePlayerNames());
                    break;
                case "debug":
                    completions.addAll(Arrays.asList("on", "off"));
                    break;
                case "border":
                    completions.addAll(worldManager.getWorldList());
                    break;
                case "sticky":
                    completions.addAll(Arrays.asList("enable", "disable", "status", "world"));
                    break;
                case "playerdata":
                    completions.addAll(Arrays.asList("list", "clear", "player"));
                    break;
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "create":
                    completions.addAll(Arrays.asList(WorldTypes.getValidTypes()));
                    break;
                case "rename":
                    // No completion for new world name (user input)
                    break;
                case "tp":
                    // Third argument for tp is target player
                    completions.addAll(getOnlinePlayerNames());
                    break;
                case "difficulty":
                    // Third argument for difficulty is target player
                    completions.addAll(getOnlinePlayerNames());
                    break;
                case "set":
                    // Third argument for set is the setting type
                    completions.addAll(Arrays.asList("difficulty", "weather", "time", "pvp", "gamemode"));
                    break;
                case "setpermission":
                case "removepermission":
                    completions.addAll(Arrays.asList(PermissionHandler.ADMIN_PERMISSION, PermissionHandler.TELEPORT_PERMISSION));
                    break;
                case "sticky":
                    String stickyAction = args[1].toLowerCase();
                    if ("world".equals(stickyAction)) {
                        completions.addAll(worldManager.getWorldList());
                    }
                    break;
                case "playerdata":
                    String playerDataAction = args[1].toLowerCase();
                    if ("player".equals(playerDataAction)) {
                        completions.addAll(getOnlinePlayerNames());
                    }
                    break;
            }
        } else if (args.length == 4) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "create":
                    // Suggest common world border sizes
                    completions.addAll(Arrays.asList("1000", "2000", "5000", "10000"));
                    break;
                case "border":
                    // Suggest common world border sizes
                    completions.addAll(Arrays.asList("1000", "2000", "5000", "10000"));
                    break;
                case "set":
                    // Fourth argument for set command depends on the setting type
                    String setting = args[2].toLowerCase();
                    switch (setting) {
                        case "difficulty":
                            completions.addAll(Arrays.asList("peaceful", "easy", "normal", "hard"));
                            break;
                        case "weather":
                            completions.addAll(Arrays.asList("clear", "rain", "thunder"));
                            break;
                        case "time":
                            completions.addAll(Arrays.asList("day", "noon", "night", "midnight"));
                            break;
                        case "pvp":
                            completions.addAll(Arrays.asList("true", "false"));
                            break;
                        case "gamemode":
                            completions.addAll(Arrays.asList("survival", "creative", "adventure", "spectator"));
                            break;
                    }
                    break;
                case "sticky":
                    // Fourth argument for sticky world command
                    if (args.length >= 2 && "world".equals(args[1].toLowerCase())) {
                        completions.addAll(Arrays.asList("enable", "disable"));
                    }
                    break;
            }
        }
        
        // Filter completions based on what the user has typed
        String lastArg = args[args.length - 1].toLowerCase();
        return completions.stream()
                .filter(completion -> completion.toLowerCase().startsWith(lastArg))
                .sorted()
                .toList();
    }
    
    private List<String> getOnlinePlayerNames() {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .toList();
    }
}
