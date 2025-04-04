package me.djtmk.InfiniteBuckets.commands;

import me.djtmk.InfiniteBuckets.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    private final Main plugin;

    public Commands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) && args.length > 0 && !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ChatColor.RED + "Only players can use most inf commands!");
            return true;
        }

        Player player = (sender instanceof Player) ? (Player) sender : null;

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "water":
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "This command is for players only!");
                    return true;
                }
                if (player.hasPermission("inf.use.water")) {
                    player.getInventory().addItem(plugin.getItemManager().infiniteWaterBucket());
                    player.sendMessage(ChatColor.GREEN + "You received an Infinite Water Bucket!");
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to obtain an infinite water bucket!");
                }
                break;

            case "lava":
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "This command is for players only!");
                    return true;
                }
                if (player.hasPermission("inf.use.lava")) {
                    player.getInventory().addItem(plugin.getItemManager().infiniteLavaBucket());
                    player.sendMessage(ChatColor.GREEN + "You received an Infinite Lava Bucket!");
                } else {
                    player.sendMessage(ChatColor.RED + "You don't have permission to obtain an infinite lava bucket!");
                }
                break;

            case "reload":
                if (sender.hasPermission("inf.admin")) {
                    plugin.reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                }
                break;

            case "give":
                if (!sender.hasPermission("inf.admin")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if (args.length != 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /inf give <player> <water|lava>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player " + args[1] + " not found!");
                    return true;
                }
                switch (args[2].toLowerCase()) {
                    case "water":
                        target.getInventory().addItem(plugin.getItemManager().infiniteWaterBucket());
                        target.sendMessage(ChatColor.GREEN + "You received an Infinite Water Bucket!");
                        sender.sendMessage(ChatColor.GREEN + "Gave " + target.getName() + " an Infinite Water Bucket!");
                        break;
                    case "lava":
                        target.getInventory().addItem(plugin.getItemManager().infiniteLavaBucket());
                        target.sendMessage(ChatColor.GREEN + "You received an Infinite Lava Bucket!");
                        sender.sendMessage(ChatColor.GREEN + "Gave " + target.getName() + " an Infinite Lava Bucket!");
                        break;
                    default:
                        sender.sendMessage(ChatColor.RED + "Invalid bucket type! Use 'water' or 'lava'");
                }
                break;

            default:
                sendHelp(sender);
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Infinite Buckets Commands:");
        sender.sendMessage(ChatColor.YELLOW + "/inf water" + ChatColor.WHITE + " - Get an infinite water bucket");
        sender.sendMessage(ChatColor.YELLOW + "/inf lava" + ChatColor.WHITE + " - Get an infinite lava bucket");
        if (sender.hasPermission("inf.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "/inf reload" + ChatColor.WHITE + " - Reload configuration");
            sender.sendMessage(ChatColor.YELLOW + "/inf give <player> <water|lava>" + ChatColor.WHITE + " - Give a bucket to a player");
        }
    }
}