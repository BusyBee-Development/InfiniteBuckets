package me.djtmk.InfiniteBuckets.utils;

import me.djtmk.InfiniteBuckets.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

public final class MessageManager {

    private final Main plugin;
    private final MiniMessage miniMessage;
    private FileConfiguration messagesConfig;

    public MessageManager(Main plugin) {
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
    }

    public void send(CommandSender sender, String key, TagResolver... placeholders) {
        String messageStr = messagesConfig.getString(key, "<red>Unknown message key: " + key + "</red>");
        Component message = miniMessage.deserialize(messageStr, placeholders);
        sender.sendMessage(message);
    }
}
