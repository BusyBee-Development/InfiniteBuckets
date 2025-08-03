package me.djtmk.InfiniteBuckets.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.*;
import me.djtmk.InfiniteBuckets.Main;
import me.djtmk.InfiniteBuckets.item.InfiniteBucket;
import me.djtmk.InfiniteBuckets.utils.MessageManager;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("infinitebuckets|infb|ib")
public final class InfiniteBucketsCommand extends BaseCommand {

    private final Main plugin;
    private final MessageManager messages;

    public InfiniteBucketsCommand(Main plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessageManager();
    }

    @Subcommand("reload")
    @CommandPermission("infb.admin")
    @Description("Reloads the plugin's configuration files.")
    public void onReload(CommandSender sender) {
        plugin.reload();
        messages.send(sender, "reload-success");
    }

    @Subcommand("give")
    @CommandPermission("infb.admin")
    @Description("Gives an infinite bucket to a player.")
    @CommandCompletion("@players @buckets @range:1-64")
    public void onGive(CommandSender sender, Player target, InfiniteBucket bucket, @Default("1") int amount) {
        ItemStack item = bucket.createItem(amount);
        target.getInventory().addItem(item);

        messages.send(sender, "give.sender",
                Placeholder.unparsed("player", target.getName()),
                Placeholder.unparsed("amount", String.valueOf(amount)),
                Placeholder.component("bucket_name", bucket.displayName())
        );
        messages.send(target, "give.receiver",
                Placeholder.unparsed("amount", String.valueOf(amount)),
                Placeholder.component("bucket_name", bucket.displayName())
        );
    }
}
