package net.busybee.InfiniteBuckets.commands;

import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.bucket.BucketFactory;
import net.busybee.InfiniteBuckets.bucket.BucketTemplate;
import net.busybee.InfiniteBuckets.inventory.impl.BucketListGUI;
import net.busybee.InfiniteBuckets.utils.DebugLogger;
import net.busybee.InfiniteBuckets.utils.MessageManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public final class InfiniteBucketsCommand implements CommandExecutor, TabCompleter {

    private final Main plugin;
    private final MessageManager messages;
    private final DebugLogger debugLogger;

    public InfiniteBucketsCommand(@NotNull Main plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessageManager();
        this.debugLogger = plugin.getDebugLogger();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            handleHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "reload" -> handleReload(sender);
            case "give" -> handleGive(sender, args);
            case "list", "builder" -> handleList(sender);
            default -> handleHelp(sender);
        }

        return true;
    }

    private void handleHelp(@NotNull CommandSender sender) {
        messages.sendRaw(sender, "help-menu");
    }

    private void handleReload(@NotNull CommandSender sender) {
        if (!sender.hasPermission("infinitebuckets.admin")) {
            messages.send(sender, "no-permission-command");
            return;
        }

        plugin.reload();
        messages.send(sender, "reload-success");
        debugLogger.debug("Plugin reloaded by " + sender.getName());
    }

    private void handleList(@NotNull CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("This command can only be used by players.");
            return;
        }

        if (!player.hasPermission("infinitebuckets.admin")) {
            messages.send(sender, "no-permission-command");
            return;
        }

        new BucketListGUI().open(player);
    }

    private void handleGive(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("infinitebuckets.admin")) {
            messages.send(sender, "no-permission-command");
            return;
        }

        if (args.length < 3) {
            messages.sendRaw(sender, "help-menu");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            messages.send(sender, "give.player-not-found", Placeholder.unparsed("player", args[1]));
            return;
        }

        Optional<BucketTemplate> templateOpt = plugin.getBucketRegistry().getTemplate(args[2]);
        if (templateOpt.isEmpty()) {
            messages.send(sender, "give.invalid-bucket", Placeholder.unparsed("bucket", args[2]));
            return;
        }

        BucketTemplate template = templateOpt.get();

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
                if (amount <= 0) {
                    messages.send(sender, "give.invalid-amount");
                    return;
                }
            } catch (NumberFormatException e) {
                messages.send(sender, "give.invalid-amount");
                return;
            }
        }

        ItemStack item = BucketFactory.createBucket(template);
        if (item != null) {
            item.setAmount(amount);
            target.getInventory().addItem(item);
            debugLogger.debug("Gave " + amount + "x " + template.getId() + " to " + target.getName() + " by " + sender.getName());

            messages.send(sender, "give.sender",
                    Placeholder.unparsed("player", target.getName()),
                    Placeholder.unparsed("amount", String.valueOf(amount)),
                    Placeholder.parsed("bucket_name", template.getDisplayName())
            );
            messages.send(target, "give.receiver",
                    Placeholder.unparsed("amount", String.valueOf(amount)),
                    Placeholder.parsed("bucket_name", template.getDisplayName())
            );
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("infinitebuckets.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], List.of("give", "reload", "help", "list", "builder"), new ArrayList<>());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return null;
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            List<String> bucketIds = plugin.getBucketRegistry().getRegisteredTemplates().stream()
                    .map(BucketTemplate::getId)
                    .collect(Collectors.toList());
            return StringUtil.copyPartialMatches(args[2], bucketIds, new ArrayList<>());
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("give")) {
            return StringUtil.copyPartialMatches(args[3], List.of("1", "16", "32", "64"), new ArrayList<>());
        }

        return Collections.emptyList();
    }
}
