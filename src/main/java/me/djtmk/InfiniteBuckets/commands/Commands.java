package me.djtmk.InfiniteBuckets.commands;

import me.djtmk.InfiniteBuckets.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Commands implements CommandExecutor, TabCompleter {

    private final Main plugin;

    public Commands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                if (sender.hasPermission("infb.admin")) {
                    plugin.reloadConfig();
                    plugin.setDebugEnabled(plugin.getConfig().getBoolean("debug", false));
                    plugin.updateAllConfigs();
                    sender.sendMessage(ChatColor.GREEN + "Configuration reloaded successfully.");
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
                        giveItem(target, waterBucket, amount, "Water", sender);
                        sender.sendMessage(ChatColor.GREEN + "Gave " + target.getName() + " " + amount + " Infinite Water Bucket(s)!");
                        break;
                    case "lava":
                        if (!target.hasPermission("infb.use.lava")) {
                            sender.sendMessage(ChatColor.RED + target.getName() + " doesn't have permission to use lava buckets!");
                            return true;
                        }
                        ItemStack lavaBucket = plugin.getItemManager().infiniteLavaBucket();
                        lavaBucket.setAmount(amount);
                        giveItem(target, lavaBucket, amount, "Lava", sender);
                        sender.sendMessage(ChatColor.GREEN + "Gave " + target.getName() + " " + amount + " Infinite Lava Bucket(s)!");
                        break;
                    default:
                        sender.sendMessage(ChatColor.RED + "Invalid bucket type! Use 'water' or 'lava'");
                }
                break;

            case "debug":
                if (!sender.hasPermission("infb.admin")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
                    return true;
                }
                if (args.length != 2 || (!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off"))) {
                    sender.sendMessage(ChatColor.RED + "Usage: /infb debug <on|off>");
                    return true;
                }
                boolean enableDebug = args[1].equalsIgnoreCase("on");
                plugin.setDebugEnabled(enableDebug);
                sender.sendMessage(ChatColor.GREEN + "Debug mode " + (enableDebug ? "enabled" : "disabled") + ".");
                if (enableDebug) {
                    sender.sendMessage(ChatColor.YELLOW + "Note: Debug mode may generate a lot of logs. Use with caution.");
                }
                break;

            default:
                sendHelp(sender);
        }

        return true;
    }

    private void giveItem(Player target, ItemStack item, int amount, String type, CommandSender sender) {
        if (target.getInventory().firstEmpty() == -1) {
            target.getWorld().dropItemNaturally(target.getLocation(), item);
            target.sendMessage(ChatColor.YELLOW + "Inventory full! Dropped " + amount + " Infinite " + type + " Bucket(s)!");
            sender.sendMessage(ChatColor.YELLOW + target.getName() + "'s inventory was full. Dropped " + amount + " Infinite " + type + " Bucket(s)!");
        } else {
            target.getInventory().addItem(item);
            target.sendMessage(ChatColor.GREEN + "You received " + amount + " Infinite " + type + " Bucket(s)!");
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Infinite Buckets Commands:");
        if (sender.hasPermission("infb.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "/infb reload" + ChatColor.WHITE + " - Reload configuration");
            sender.sendMessage(ChatColor.YELLOW + "/infb give <player> <water|lava> <amount>" + ChatColor.WHITE + " - Give buckets to a player");
            sender.sendMessage(ChatColor.YELLOW + "/infb debug <on|off>" + ChatColor.WHITE + " - Toggle debug mode");
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Ask an admin for infinite buckets!");
        }
    }

    private void sendInvalidAmount(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Amount must be a positive number!");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, @NotNull Command command, @NotNull String alias, String @NotNull [] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("infb.admin")) return completions;

        if (args.length == 1) {
            completions.addAll(Arrays.asList("reload", "give", "debug"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .toList());
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(Arrays.asList("water", "lava"));
        } else if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            completions.addAll(Arrays.asList("1", "2", "3", "4", "5"));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("debug")) {
            completions.addAll(Arrays.asList("on", "off"));
        }

        return completions.stream()
                .filter(c -> c.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}
