package me.djtmk.InfiniteBuckets.commands;

import me.djtmk.InfiniteBuckets.Main;
import me.djtmk.InfiniteBuckets.utils.ConfigKey;
import me.djtmk.InfiniteBuckets.utils.StringUtils;
import org.bukkit.Bukkit;
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
                    plugin.setDebugEnabled(ConfigKey.DEBUG.getBoolean(plugin, false));
                    plugin.updateAllConfigs();
                    sender.sendMessage(StringUtils.color("&aConfiguration reloaded successfully."));
                } else {
                    sender.sendMessage(StringUtils.color("&cYou do not have permission to use this command."));
                }
                break;

            case "give":
                if (!sender.hasPermission("infb.admin")) {
                    sender.sendMessage(StringUtils.color("&cYou do not have permission to use this command."));
                    return true;
                }
                if (args.length != 4) {
                    sender.sendMessage(StringUtils.color("&cUsage: /infb give <player> <water|lava> <amount>"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(StringUtils.color("&cPlayer " + args[1] + " not found!"));
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
                            sender.sendMessage(StringUtils.color("&c" + target.getName() + " doesn't have permission to use water buckets!"));
                            return true;
                        }
                        ItemStack waterBucket = plugin.getItemManager().infiniteWaterBucket();
                        waterBucket.setAmount(amount);
                        giveItem(target, waterBucket, amount, "Water", sender);
                        sender.sendMessage(StringUtils.color("&aGave " + target.getName() + " " + amount + " Infinite Water Bucket(s)!"));
                        break;
                    case "lava":
                        if (!target.hasPermission("infb.use.lava")) {
                            sender.sendMessage(StringUtils.color("&c" + target.getName() + " doesn't have permission to use lava buckets!"));
                            return true;
                        }
                        ItemStack lavaBucket = plugin.getItemManager().infiniteLavaBucket();
                        lavaBucket.setAmount(amount);
                        giveItem(target, lavaBucket, amount, "Lava", sender);
                        sender.sendMessage(StringUtils.color("&aGave " + target.getName() + " " + amount + " Infinite Lava Bucket(s)!"));
                        break;
                    default:
                        sender.sendMessage(StringUtils.color("&cInvalid bucket type! Use 'water' or 'lava'"));
                }
                break;

            case "debug":
                if (!sender.hasPermission("infb.admin")) {
                    sender.sendMessage(StringUtils.color("&cYou do not have permission to use this command."));
                    return true;
                }
                if (args.length != 2 || (!args[1].equalsIgnoreCase("on") && !args[1].equalsIgnoreCase("off"))) {
                    sender.sendMessage(StringUtils.color("&cUsage: /infb debug <on|off>"));
                    return true;
                }
                boolean enableDebug = args[1].equalsIgnoreCase("on");
                plugin.setDebugEnabled(enableDebug);
                sender.sendMessage(StringUtils.color("&aDebug mode " + (enableDebug ? "enabled" : "disabled") + "."));
                if (enableDebug) {
                    sender.sendMessage(StringUtils.color("&eNote: Debug mode may generate a lot of logs. Use with caution."));
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
            target.sendMessage(StringUtils.color("&eInventory full! Dropped " + amount + " Infinite " + type + " Bucket(s)!"));
            sender.sendMessage(StringUtils.color("&e" + target.getName() + "'s inventory was full. Dropped " + amount + " Infinite " + type + " Bucket(s)!"));
        } else {
            target.getInventory().addItem(item);
            target.sendMessage(StringUtils.color("&aYou received " + amount + " Infinite " + type + " Bucket(s)!"));
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(StringUtils.color("&6Infinite Buckets Commands:"));
        if (sender.hasPermission("infb.admin")) {
            sender.sendMessage(StringUtils.color("&e/infb reload &f- Reload configuration"));
            sender.sendMessage(StringUtils.color("&e/infb give <player> <water|lava> <amount> &f- Give buckets to a player"));
            sender.sendMessage(StringUtils.color("&e/infb debug <on|off> &f- Toggle debug mode"));
        } else {
            sender.sendMessage(StringUtils.color("&eAsk an admin for infinite buckets!"));
        }
    }

    private void sendInvalidAmount(CommandSender sender) {
        sender.sendMessage(StringUtils.color("&cAmount must be a positive number!"));
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
