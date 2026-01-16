package me.djtmk.InfiniteBuckets.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.receiver.Message;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.HytaleServer;
import me.djtmk.InfiniteBuckets.InfiniteBuckets;

public class InfBCommand extends CommandBase {

    private final InfiniteBuckets plugin;

    public InfBCommand(InfiniteBuckets plugin) {
        super("infb");
        this.plugin = plugin;
        addAliases("infinitebuckets");
    }

    @Override
    public void executeSync(CommandContext context) {
        CommandSender sender = context.getSender();
        String[] args = context.getArguments();

        if (args.length >= 1) {
            PlayerRef player = sender.asPlayer();
            if (player == null) {
                sender.sendMessage(Message.text("§cOnly players can use this command."));
                return;
            }

            String type = args[0].toLowerCase();
            if (!type.equals("water") && !type.equals("lava")) {
                usage(sender);
                return;
            }

            giveBucket(player, type);
            return;
        }
        usage(sender);
    }

    private void giveBucket(PlayerRef player, String type) {
        String itemId = type.equals("lava") ? "hytale:lava_bucket" : "hytale:water_bucket";
        ItemStack item = ItemStack.create(itemId, 1);

        // Flag item as infinite using metadata
        item.setMetadata("inf_bucket", true, HytaleServer.get().getCodecRegistry());

        Inventory inv = player.getEntity().get(Inventory.class);
        if (inv != null) {
            inv.getHotbar().add(item);
            player.sendMessage(Message.text("§aYou received an infinite bucket!"));
        }
    }

    private void usage(CommandSender sender) {
        sender.sendMessage(Message.text("§7Usage: /infb <water|lava>"));
    }
}
