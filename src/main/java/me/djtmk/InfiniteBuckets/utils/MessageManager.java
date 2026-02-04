package me.djtmk.InfiniteBuckets.utils;

import me.djtmk.InfiniteBuckets.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public final class MessageManager {

    private final Main plugin;
    private final MiniMessage miniMessage;
    private FileConfiguration messagesConfig;
    private Component prefix;

    public FileConfiguration getMessagesConfig() {
        return this.messagesConfig;
    }

    public MessageManager(@NotNull Main plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.loadMessages();
    }

    public void reload() {
        this.loadMessages();
    }

    private void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        this.messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

        String prefixString = messagesConfig.getString("plugin-prefix", "<gray>[<aqua>InfiniteBuckets</aqua>]</gray>");
        this.prefix = miniMessage.deserialize(prefixString + " <gray>»</gray> ");
    }

    public void send(@NotNull CommandSender sender, @NotNull String key, @NotNull TagResolver... placeholders) {
        boolean onlyUpdateMessages = plugin.getConfig().getBoolean("messages.only-update-messages", false);
        if (onlyUpdateMessages && !key.equals("update-notifier")) {
            return;
        }

        String messageStr = messagesConfig.getString(key, "<red>Unknown message key: " + key + "</red>");
        if (messageStr == null || messageStr.trim().isEmpty()) {
            return;
        }
        Component message = miniMessage.deserialize(messageStr, placeholders);
        sender.sendMessage(prefix.append(message));
    }

    public void sendRaw(@NotNull CommandSender sender, @NotNull String key) {
        boolean onlyUpdateMessages = plugin.getConfig().getBoolean("messages.only-update-messages", false);
        if (onlyUpdateMessages && !key.equals("update-notifier")) {
            return;
        }

        List<String> messageLines = messagesConfig.getStringList(key);
        for (String line : messageLines) {
            sender.sendMessage(miniMessage.deserialize(line));
        }
    }
}
