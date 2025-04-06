package me.djtmk.InfiniteBuckets.commands;

import me.djtmk.InfiniteBuckets.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Commands implements CommandExecutor {

    private final Main plugin;

    public Commands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (sender.hasPermission("infb.admin")) {
                    plugin.reloadConfig();
                    sender.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
                } else {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                }
                break;

            case "give":
                if (!sender.hasPermission("infb.admin")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if (args.length != 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /infb give <player> <water|lava> <amount>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Player " + args[1] + " not found!");
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[3]);
                    if (amount <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    sendInvalidAmount(sender);
                    return true;
                }

                switch (args[2].toLowerCase()) {
                    case "water":
                        if (!target.hasPermission("infb.use.water")) {
                            sender.sendMessage(ChatColor.RED + target.getName() + " doesn't have permission to use water buckets!");
                            return true;
                        }
                        ItemStack waterBucket = plugin.getItemManager().infiniteWaterBucket();
                        waterBucket.setAmount(amount);
                        target.getInventory().addItem(waterBucket);
                        target.sendMessage(ChatColor.GREEN + "You received " + amount + " Infinite Water Bucket(s)!");
                        sender.sendMessage(ChatColor.GREEN + "Gave " + target.getName() + " " + amount + " Infinite Water Bucket(s)!");
                        break;
                    case "lava":
                        if (!target.hasPermission("infb.use.lava")) {
                            sender.sendMessage(ChatColor.RED + target.getName() + " doesn't have permission to use lava buckets!");
                            return true;
                        }
                        ItemStack lavaBucket = plugin.getItemManager().infiniteLavaBucket();
                        lavaBucket.setAmount(amount);
                        target.getInventory().addItem(lavaBucket);
                        target.sendMessage(ChatColor.GREEN + "You received " + amount + " Infinite Lava Bucket(s)!");
                        sender.sendMessage(ChatColor.GREEN + "Gave " + target.getName() + " " + amount + " Infinite Lava Bucket(s)!");
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
        if (sender.hasPermission("infb.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "/infb reload" + ChatColor.WHITE + " - Reload configuration");
            sender.sendMessage(ChatColor.YELLOW + "/infb give <player> <water|lava> <amount>" + ChatColor.WHITE + " - Give buckets to a player");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Ask an admin for infinite buckets!");
        }
    }

    private void sendInvalidAmount(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Amount must be a positive number!");
    }
}