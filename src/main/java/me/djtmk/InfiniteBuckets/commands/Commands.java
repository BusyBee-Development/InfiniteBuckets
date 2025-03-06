package me.djtmk.InfiniteBuckets.commands;

import me.djtmk.InfiniteBuckets.Main;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;

public class Commands implements CommandExecutor {

    private final Main plugin;

    public Commands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use that command!");
            return true;
        }

        Player player = (Player) sender;
        Economy economy = plugin.getEconomy();
        DecimalFormat formatter = new DecimalFormat("#,###");

        switch (cmd.getName().toLowerCase()) {
            case "infwater":
                handleInfiniteBucketPurchase(player, args, "water", plugin.getConfig().getInt("water.cost"), economy, formatter, plugin.getItemManager().infiniteWaterBucket());
                break;

            case "inflava":
                handleInfiniteBucketPurchase(player, args, "lava", plugin.getConfig().getInt("lava.cost"), economy, formatter, plugin.getItemManager().infiniteLavaBucket());
                break;

            case "infinitebuckets":
                if (player.hasPermission("infbuckets.admin")) {
                    handleAdminCommand(player, args);
                } else {
                    player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                }
                break;

            default:
                player.sendMessage(ChatColor.RED + "Unknown command!");
        }

        return true;
    }

    private void handleInfiniteBucketPurchase(Player player, String[] args, String type, int cost, Economy economy, DecimalFormat formatter, Object bucket) {
        if (!player.hasPermission("infbuckets.obtain")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to buy an infinite " + type + " bucket!");
            return;
        }

        if (args.length == 0) {
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "You are about to purchase an infinite " + type + " bucket");
            player.sendMessage(ChatColor.GREEN + "This costs " + formatter.format(cost) + ". Type /inf" + type + " confirm to buy it.");
            player.sendMessage("");
        } else if (args.length == 1 && args[0].equalsIgnoreCase("confirm")) {
            if (economy.getBalance(player) >= cost) {
                economy.withdrawPlayer(player, cost);
                player.getInventory().addItem((org.bukkit.inventory.ItemStack) bucket);
                player.sendMessage(ChatColor.GREEN + "You have bought an Infinite " + type + " bucket!");
            } else {
                player.sendMessage(ChatColor.RED + "You do not have enough money!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Usage: /inf" + type + " [confirm]");
        }
    }

    private void handleAdminCommand(Player player, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            player.sendMessage(ChatColor.GREEN + "Configuration reloaded.");
        } else {
            player.sendMessage(ChatColor.RED + "Usage: /infinitebuckets reload");
        }
    }
}