package net.busybee.InfiniteBuckets.inventory.impl;

import net.busybee.InfiniteBuckets.Main;
import net.busybee.InfiniteBuckets.bucket.BucketTemplate;
import net.busybee.InfiniteBuckets.utils.MessageManager;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatPromptListener implements Listener {

    private static final Map<UUID, PromptData> activePrompts = new HashMap<>();

    public enum PromptType {
        NAME, LORE
    }

    public record PromptData(BucketTemplate template, PromptType type) {}

    public static void startPrompt(Player player, BucketTemplate template, PromptType type) {
        activePrompts.put(player.getUniqueId(), new PromptData(template, type));
        String key = type == PromptType.NAME ? "gui.prompts.enter-name" : "gui.prompts.enter-lore";
        Main.getInstance().getMessageManager().send(player, key);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        PromptData data = activePrompts.remove(player.getUniqueId());

        if (data == null) return;

        event.setCancelled(true);
        String message = event.getMessage();

        if (message.equalsIgnoreCase("cancel")) {
            Main.getInstance().getMessageManager().send(player, "gui.prompts.cancelled");
            Main.scheduler().runNextTick(p -> new BucketBuilderGUI(data.template()).open(player));
            return;
        }

        if (data.type() == PromptType.NAME) {
            String processed = MessageManager.legacyToMiniMessage(message);
            try {
                MiniMessage.miniMessage().deserialize(processed);
                data.template().setDisplayName(processed);
                Main.getInstance().getMessageManager().send(player, "gui.prompts.name-updated", Placeholder.parsed("name", processed));
            } catch (Exception e) {
                Main.getInstance().getMessageManager().send(player, "gui.prompts.invalid-format");
                startPrompt(player, data.template(), data.type());
                return;
            }
        } else if (data.type() == PromptType.LORE) {
            String processed = MessageManager.legacyToMiniMessage(message);
            try {
                for (String line : processed.split("\\|")) {
                    MiniMessage.miniMessage().deserialize(line);
                }
                data.template().setLore(Arrays.asList(processed.split("\\|")));
                Main.getInstance().getMessageManager().send(player, "gui.prompts.lore-updated");
            } catch (Exception e) {
                Main.getInstance().getMessageManager().send(player, "gui.prompts.invalid-format");
                startPrompt(player, data.template(), data.type());
                return;
            }
        }

        Main.scheduler().runNextTick(p -> new BucketBuilderGUI(data.template()).open(player));
    }
}
