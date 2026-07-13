package org.xpfarm.worldcrud;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.PluginManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PermissionHandler {
    
    private final WorldCRUDPlugin plugin;
    private final Map<Player, PermissionAttachment> attachments;
    
    // Permission constants
    public static final String ADMIN_PERMISSION = "worldcrud.admin";
    public static final String TELEPORT_PERMISSION = "worldcrud.teleport";
    
    public PermissionHandler(WorldCRUDPlugin plugin) {
        this.plugin = plugin;
        this.attachments = new HashMap<>();
        
        // Register permissions
        registerPermissions();
    }
    
    private void registerPermissions() {
        PluginManager pm = plugin.getServer().getPluginManager();
        
        // Register admin permission if not exists
        if (pm.getPermission(ADMIN_PERMISSION) == null) {
            Permission adminPerm = new Permission(ADMIN_PERMISSION, "Full access to all WorldCRUD commands");
            pm.addPermission(adminPerm);
        }
        
        // Register teleport permission if not exists
        if (pm.getPermission(TELEPORT_PERMISSION) == null) {
            Permission teleportPerm = new Permission(TELEPORT_PERMISSION, "Allows teleporting to registered worlds");
            pm.addPermission(teleportPerm);
        }
    }
    
    public boolean hasPermission(CommandSender sender, String permission) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            return player.hasPermission(permission) || player.isOp();
        }
        // Console always has permission
        return true;
    }
    
    public boolean hasAdminPermission(CommandSender sender) {
        return hasPermission(sender, ADMIN_PERMISSION);
    }
    
    public boolean hasTeleportPermission(CommandSender sender) {
        return hasPermission(sender, TELEPORT_PERMISSION);
    }
    
    public void addPermission(Player player, String permission) {
        PermissionAttachment attachment = getAttachment(player);
        attachment.setPermission(permission, true);
        plugin.logDebug("Added permission " + permission + " to " + player.getName());
    }
    
    public void removePermission(Player player, String permission) {
        PermissionAttachment attachment = getAttachment(player);
        attachment.unsetPermission(permission);
        plugin.logDebug("Removed permission " + permission + " from " + player.getName());
    }
    
    public Set<String> getPlayerPermissions(Player player) {
        PermissionAttachment attachment = getAttachment(player);
        return attachment.getPermissions().keySet();
    }
    
    public void removePlayer(Player player) {
        PermissionAttachment attachment = attachments.remove(player);
        if (attachment != null) {
            attachment.remove();
            plugin.logDebug("Removed permission attachment for " + player.getName());
        }
    }
    
    public void sendNoPermissionMessage(CommandSender sender, String permission) {
        plugin.sendMessage(sender, Component.text("You don't have permission to use this command.", NamedTextColor.RED)
                .append(Component.text(" Required: ", NamedTextColor.GRAY))
                .append(Component.text(permission, NamedTextColor.YELLOW)));
    }
    
    public void showPermissionStructure(CommandSender sender) {
        sender.sendMessage(Component.text("=== WorldCRUD Permissions ===", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("worldcrud.admin", NamedTextColor.YELLOW)
                .append(Component.text(" - Full access to all commands", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("worldcrud.teleport", NamedTextColor.YELLOW)
                .append(Component.text(" - Allows teleporting to worlds", NamedTextColor.GRAY)));
        
        if (sender instanceof Player) {
            Player player = (Player) sender;
            sender.sendMessage(Component.text("Your permissions:", NamedTextColor.AQUA));
            
            if (hasAdminPermission(sender)) {
                sender.sendMessage(Component.text("✓ worldcrud.admin", NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("✗ worldcrud.admin", NamedTextColor.RED));
            }
            
            if (hasTeleportPermission(sender)) {
                sender.sendMessage(Component.text("✓ worldcrud.teleport", NamedTextColor.GREEN));
            } else {
                sender.sendMessage(Component.text("✗ worldcrud.teleport", NamedTextColor.RED));
            }
        }
    }
    
    private PermissionAttachment getAttachment(Player player) {
        PermissionAttachment attachment = attachments.get(player);
        if (attachment == null) {
            attachment = player.addAttachment(plugin);
            attachments.put(player, attachment);
        }
        return attachment;
    }
}
